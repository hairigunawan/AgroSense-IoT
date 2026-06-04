import re

with open('app/src/main/res/layout/activity_weather_modern.xml', 'r', encoding='utf-8') as f:
    content = f.read()

replacements = [
    (r'android:text="27°"', r'tools:text="27°"\n                android:text="--°"'),
    (r'android:text="Berawan"', r'tools:text="Berawan"\n                android:text="--"'),
    (r'android:text="T: 32°  R: 24°"', r'tools:text="T: 32°  R: 24°"\n                android:text="--"'),
    (r'android:text="Terasa seperti 32°"', r'tools:text="Terasa seperti 32°"\n                android:text="--"'),
    (r'android:text="Hari yang cukup lembap dengan potensi hujan ringan di sore hari."', r'tools:text="Hari yang cukup lembap dengan potensi hujan ringan di sore hari."\n                android:text=""'),
    (r'android:text="Hujan Segera Turun"', r'tools:text="Hujan Segera Turun"\n                android:text="--"'),
    (r'android:text="Sekitar 15 menit lagi"', r'tools:text="Sekitar 15 menit lagi"\n                android:text="--"'),
    (r'android:text="42%"', r'tools:text="42%"\n                android:text="--%"'),
]

for old, new in replacements:
    content = re.sub(old, new, content)

with open('app/src/main/res/layout/activity_weather_modern.xml', 'w', encoding='utf-8') as f:
    f.write(content)

with open('app/src/main/res/layout/item_daily_modern.xml', 'r', encoding='utf-8') as f:
    content_daily = f.read()

replacements_daily = [
    (r'android:text="Senin"', r'tools:text="Senin"\n        android:text="--"'),
    (r'android:text="40%"', r'tools:text="40%"\n        android:text="--"'),
    (r'android:text="32° / 24°"', r'tools:text="32° / 24°"\n        android:text="--"'),
]

for old, new in replacements_daily:
    content_daily = re.sub(old, new, content_daily)

with open('app/src/main/res/layout/item_daily_modern.xml', 'w', encoding='utf-8') as f:
    f.write(content_daily)

print('Done')
