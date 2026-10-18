# HomeNest 🏡
**Smarte Haushalts- & Familien-App für Android** (Kotlin + Jetpack Compose)

[![Build Android APK](https://github.com/MeisterGamer/Home-App/actions/workflows/build-apk.yml/badge.svg)](https://github.com/MeisterGamer/Home-App/actions/workflows/build-apk.yml)

HomeNest vereinfacht das Familienleben durch die intelligente Verwaltung von Haushaltsaufgaben, dringenden Reparaturen, Einkaufslisten, gemeinsamen Kalendern, Haushaltsbudgets, Smart-Home-Geräten und einem speziellen Senioren-Modus.

---

## ⚠️ Hinweis zu PWABuilder
**Warum meldet PWABuilder einen Fehler?**
- **PWABuilder** ist ausschließlich für *gehostete Webseiten* (Progressive Web Apps / HTML & Service Worker) gedacht.
- Eine GitHub-URL (`https://github.com/...`) ist **keine Website**, sondern ein Quellcode-Repository.
- **Vorteil hier:** Diese App ist bereits eine **echte, native Android-App (Kotlin + Jetpack Compose + SQLite Room)**! Du brauchst **keinen** PWABuilder.

---

## 📲 So installierst du die App auf deinem Android-Handy

### Option 1: Direkt aus AI Studio (Sofort)
1. Klicke im AI Studio Menü oben rechts auf **Settings / Export**.
2. Wähle **Download APK** oder **Push to GitHub**.
3. Installiere die `.apk`-Datei direkt auf deinem Android-Smartphone.

### Option 2: Automatisch über GitHub Actions (CI/CD)
1. Sobald dieser Code auf dein GitHub-Repository [`MeisterGamer/Home-App`](https://github.com/MeisterGamer/Home-App) gepusht wird, startet GitHub Actions automatisch den Workflow `.github/workflows/build-apk.yml`.
2. Unter dem Reiter **Actions** in GitHub wird die Datei `HomeNest-Android-App-APK` (.apk) automatisch kompiliert und bereitgestellt.
3. Du kannst die APK herunterladen und auf jedem Smartphone installieren!

---

## ✨ Hauptfunktionen

1. **Aufgaben & Dringende Reparaturen (Urgent Household Repairs)**
   - Tagesfokus mit Fortschrittsbalken und Punkten.
   - Auffälliges Warnbanner für dringende Reparaturen (z. B. Wasserrohrbruch, Heizungsfilterwechsel).
   - Kategorisierte Aufgaben: Küche, Reinigung, Wartung, Garten, Haustiere, Sicherheit.

2. **Gemeinsame Einkaufsliste & Familienkalender**
   - Automatische Gesamtkostenberechnung ($/€).
   - Schnellauswahl für Haushalts-Klassiker (Milch, Brot, Filter, Tabs).
   - Kalender-Tagesansicht mit Aufgaben, Reparaturterminen und Medikamentenzeiten.

3. **Integriertes Benachrichtigungssystem**
   - Android-Benachrichtigungskanäle für dringende Alarme (hohe Priorität & Vibration) und Medikamentenerinnerungen.

4. **Automatisiertes Monatsbudget**
   - Monatslimit (1.200 € / $) mit Auslastungsanzeige.
   - Automatische Kategorisierung (Wartung, Lebensmittel, Nebenkosten, Drogerie).
   - Schnelle Belegerfassung.

5. **Rollen & Berechtigungen (RBAC)**
   - **Eltern / Admin**: Voller Zugriff auf Finanzen, Rollen, Geräte und Aufgaben.
   - **Jugendliche**: Eigene Aufgaben, Kalender, Einkaufsliste (Budget geschützt).
   - **Kinder**: Spielerische Aufgabenliste mit Sternen und Punkten.
   - **Senioren / Großeltern**: Barrierefreier Großschrift-Modus.

6. **Senioren- & Pflegemodus (Große Schrift & Hoher Kontrast)**
   - Große Schaltflächen (>64dp) und kontrastreiche Lesbarkeit.
   - **Notruf-Taste**: Direkter 1-Klick-Anruf bei Familienangehörigen.
   - **Medikamentenplan**: Übersicht mit großen „Eingenommen ✓“-Tasten.
   - **Wearable- & Vitalwert-Monitor**: Puls (BPM), Schritte, Schlafdauer und Sturzüberwachung.

7. **Smart-Home-Integration**
   - Telemetrie für Saugroboter, Geschirrspüler, HVAC-Luftfilter, Rauchmelder und Smart-Kühlschrank.
   - Automatische Aufgabenerstellung (z. B. Spülmaschine fertig ➔ Aufgabe „Spülmaschine ausräumen“ wird automatisch Alex zugewiesen).

8. **Sprachsteuerung (Hands-Free Voice Commands)**
   - Natürliche Sprachbefehle wie:
     - *„Milch auf die Einkaufsliste setzen“*
     - *„Geschirr erledigt“*
     - *„Dringende Reparatur Wasserrohr Küche“*
     - *„45 Euro für Lebensmittel erfassen“*
     - *„Seniorenmodus aktivieren“*

9. **100% Offline-Fähig & Cloud-Backup**
   - Lokale SQLite Room-Datenbank: Funktioniert komplett ohne Internet.
   - Cloud-Sync-Manager zur Datensicherung und Wiederherstellung auf mehreren Geräten.

10. **Mehrsprachigkeit**
    - Unterstützt Deutsch 🇩🇪, Englisch 🇺🇸, Spanisch 🇪🇸, Französisch 🇫🇷, Italienisch 🇮🇹 und Chinesisch 🇨🇳.

---

## 🛠️ Technische Details
- **Plattform:** Android (minSdk 24, targetSdk 36)
- **Sprache:** Kotlin 2.2+
- **UI:** Jetpack Compose & Material Design 3
- **Lokale Datenbank:** Android Room SQLite
- **Architektur:** MVVM + Repository Pattern + StateFlow
