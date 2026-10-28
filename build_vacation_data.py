import json
import re
from datetime import datetime

with open(r'C:\Users\Delkira\Downloads/IVSS/vacaciones_merged.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

all_workers = data['all']

admin_vacations = []
item_id = 1

# Current comparison reference: Feb 2025
ref_date = datetime(2025, 2, 1)

def parse_end_date_and_reintegro(range_str):
    if not range_str or range_str in ['None', 'None/None', '', 'disfrutadas']:
        if range_str == 'disfrutadas':
            return "Disfrutada", "Completado"
        return "Pendiente por Disfrutar", "Pendiente"
    
    # Clean range string
    clean_str = range_str.replace(' ', '')
    # Match dates like 26/10/21 or 26/10/2021 or 15/08/2024
    parts = clean_str.split('-')
    if len(parts) >= 2:
        end_part = parts[1].strip()
        # Extract day, month, year
        match = re.search(r'(\d{1,2})[/.-](\d{1,2})[/.-](\d{2,4})', end_part)
        if match:
            day, month, year = int(match.group(1)), int(match.group(2)), int(match.group(3))
            if year < 100:
                year += 2000
            try:
                end_dt = datetime(year, month, day)
                # Fecha de reintegro es el día siguiente
                reintegro_str = f"{day+1:02d}/{month:02d}/{year}"
                if end_dt < ref_date:
                    return "Disfrutada", reintegro_str
                else:
                    return "En Disfrute", reintegro_str
            except Exception:
                pass
    return "En Disfrute", range_str

for w in all_workers:
    cedula = w['cedula']
    name = w['name']
    cargo = w['cargo']
    periods = w['periods']
    
    if not periods:
        admin_vacations.append({
            'id': item_id,
            'workerName': name,
            'workerCedula': cedula,
            'periodName': 'Vacaciones Período 2024 - 2025',
            'status': 'Pendiente por Disfrutar',
            'fechaReintegro': 'Pendiente',
            'usedDays': 0,
            'totalDays': 15,
            'fileName': f"Forma_12-16_{name.replace(' ', '_')}.docx"
        })
        item_id += 1
    else:
        for p in periods:
            p_name = p['period']
            p_val = p['dates_or_status']
            
            if '20' in p_name and '-' in p_name:
                p_title = f"Vacaciones Período {p_name.strip()}"
            elif p_name:
                p_title = f"Vacaciones {p_name.strip()}"
            else:
                p_title = "Vacaciones Período 2024 - 2025"
                
            status, reintegro = parse_end_date_and_reintegro(p_val)
            
            used = 15 if status in ["Disfrutada", "En Disfrute"] else 0
            clean_filename = f"Forma_12-16_{name.replace(' ', '_')}.docx"
            
            admin_vacations.append({
                'id': item_id,
                'workerName': name,
                'workerCedula': cedula,
                'periodName': p_title,
                'status': status,
                'fechaReintegro': reintegro,
                'usedDays': used,
                'totalDays': 15,
                'fileName': clean_filename
            })
            item_id += 1

# Count by status
disfrutadas_count = sum(1 for v in admin_vacations if v['status'] == 'Disfrutada')
en_disfrute_count = sum(1 for v in admin_vacations if v['status'] == 'En Disfrute')
pending_count = sum(1 for v in admin_vacations if v['status'] == 'Pendiente por Disfrutar')

print(f"Total vacation records: {len(admin_vacations)}")
print(f" - Disfrutadas (Completadas): {disfrutadas_count}")
print(f" - En Disfrute (Activas/Programadas): {en_disfrute_count}")
print(f" - Pendientes por Disfrutar: {pending_count}")

# Generate Kotlin file
kotlin_code = """package com.example.ivss.platform

object VacationDataHolder {
    val allVacations: List<AdminVacationItem> = listOf(
"""

for v in admin_vacations:
    worker = v['workerName'].replace('"', '\\"')
    ci = v['workerCedula']
    period = v['periodName'].replace('"', '\\"')
    st = v['status']
    reintegro = v['fechaReintegro'].replace('"', '\\"')
    used = v['usedDays']
    tot = v['totalDays']
    fname = v['fileName'].replace('"', '\\"')
    kotlin_code += f'        AdminVacationItem({v["id"]}, "{worker}", "{ci}", "{period}", "{st}", "{reintegro}", {used}, {tot}, "{fname}"),\n'

kotlin_code += """    )
}
"""

with open(r'C:\Users\Delkira\Downloads\IVSS\composeApp\src\commonMain\kotlin\com/example/ivss/platform/VacationDataHolder.kt', 'w', encoding='utf-8') as kf:
    kf.write(kotlin_code)

print("Updated VacationDataHolder.kt successfully!")
