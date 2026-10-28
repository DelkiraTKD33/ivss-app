import json

with open(r'C:\Users\Delkira\Downloads/IVSS/vacaciones_merged.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

all_workers = data['all']

admin_vacations = []
item_id = 1

for w in all_workers:
    cedula = w['cedula']
    name = w['name']
    cargo = w['cargo']
    periods = w['periods']

    if not periods:
        # Default period if no explicit period listed
        admin_vacations.append({
            'id': item_id,
            'workerName': name,
            'workerCedula': cedula,
            'periodName': 'Vacaciones Período 2024 - 2025',
            'status': 'Pendiente por Disfrutar',
            'usedDays': 0,
            'totalDays': 15,
            'fileName': f"Forma_12-16_{name.replace(' ', '_')}.docx"
        })
        item_id += 1
    else:
        for p in periods:
            p_name = p['period']
            p_val = p['dates_or_status']

            # Format period name
            if '20' in p_name and '-' in p_name:
                p_title = f"Vacaciones Período {p_name.strip()}"
            elif p_name:
                p_title = f"Vacaciones {p_name.strip()}"
            else:
                p_title = "Vacaciones Período 2024 - 2025"

            # Determine status
            # If dates_or_status has dates like '27/09/21-26/10/21' or 'disfrutadas', it is 'Aprobada' (En Disfrute)
            # If empty or None or 'None', it is 'Pendiente por Disfrutar'
            if p_val and p_val not in ['None', 'None/None', ''] and 'None' not in p_val:
                status = "Aprobada"
                used = 15
            else:
                status = "Pendiente por Disfrutar"
                used = 0

            clean_filename = f"Forma_12-16_{name.replace(' ', '_')}.docx"
            admin_vacations.append({
                'id': item_id,
                'workerName': name,
                'workerCedula': cedula,
                'periodName': p_title,
                'status': status,
                'usedDays': used,
                'totalDays': 15,
                'fileName': clean_filename
            })
            item_id += 1

print(f"Generated {len(admin_vacations)} vacation records across {len(all_workers)} workers.")

# Count pending vs approved
approved_count = sum(1 for v in admin_vacations if v['status'] == 'Aprobada')
pending_count = sum(1 for v in admin_vacations if v['status'] != 'Aprobada')
print(f"Approved (En disfrute): {approved_count}, Pending: {pending_count}")

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
    used = v['usedDays']
    tot = v['totalDays']
    fname = v['fileName'].replace('"', '\\"')
    kotlin_code += f'        AdminVacationItem({v["id"]}, "{worker}", "{ci}", "{period}", "{st}", {used}, {tot}, "{fname}"),\n'

kotlin_code += """    )
}
"""

with open(r'C:\Users\Delkira\Downloads\IVSS\composeApp\src\commonMain\kotlin\com/example/ivss/platform/VacationDataHolder.kt', 'w', encoding='utf-8') as kf:
    kf.write(kotlin_code)

print("Wrote VacationDataHolder.kt successfully!")
