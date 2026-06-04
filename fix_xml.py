import re

# Update activity_about.xml
with open('app/src/main/res/layout/activity_about.xml', 'r', encoding='utf-8') as f:
    about_content = f.read()

if 'xmlns:tools=' not in about_content:
    about_content = about_content.replace('xmlns:app="http://schemas.android.com/apk/res-auto"', 'xmlns:app="http://schemas.android.com/apk/res-auto"\n    xmlns:tools="http://schemas.android.com/tools"')

about_replacements = [
    (r'android:text="Versi 1.2.3 \(Build 2024.1\)"', r'tools:text="Versi 1.2.3 (Build 2024.1)"\n                android:text=""'),
    (r'android:text="Tim Developer AgroSense"', r'tools:text="Tim Developer AgroSense"\n                                android:text=""'),
    (r'android:text="support@agrosense.id"', r'tools:text="support@agrosense.id"\n                                android:text=""'),
    (r'android:text="© 2026 AgroSense Indonesia"', r'tools:text="© 2026 AgroSense Indonesia"\n                android:text=""')
]

for old, new in about_replacements:
    about_content = re.sub(old, new, about_content)

with open('app/src/main/res/layout/activity_about.xml', 'w', encoding='utf-8') as f:
    f.write(about_content)


# Update activity_notification.xml
with open('app/src/main/res/layout/activity_notification.xml', 'r', encoding='utf-8') as f:
    notif_content = f.read()

if 'xmlns:tools=' not in notif_content:
    notif_content = notif_content.replace('xmlns:app="http://schemas.android.com/apk/res-auto"', 'xmlns:app="http://schemas.android.com/apk/res-auto"\n    xmlns:tools="http://schemas.android.com/tools"')

notif_replacements = [
    (r'android:text="Penyiraman Berhasil"', r'tools:text="Penyiraman Berhasil"\n                                android:text=""'),
    (r'android:text="Baru"', r'tools:text="Baru"\n                                android:text=""'),
    (r'android:text="Sistem telah menyiram kangkung secara otomatis."', r'tools:text="Sistem telah menyiram kangkung secara otomatis."\n                            android:text=""'),
    (r'android:text="Baru saja"', r'tools:text="Baru saja"\n                            android:text=""'),
    (r'android:text="Tangki Air Hampir Habis"', r'tools:text="Tangki Air Hampir Habis"\n                            android:text=""'),
    (r'android:text="Segera isi ulang tangki air Anda."', r'tools:text="Segera isi ulang tangki air Anda."\n                            android:text=""'),
    (r'android:text="1 jam yang lalu"', r'tools:text="1 jam yang lalu"\n                            android:text=""')
]

for old, new in notif_replacements:
    notif_content = re.sub(old, new, notif_content)

with open('app/src/main/res/layout/activity_notification.xml', 'w', encoding='utf-8') as f:
    f.write(notif_content)

print("Done")