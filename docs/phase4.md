# Phase 4 - Verlauf der Testing-Arbeit

## Ausgangspunkt

Ich begann mit dem Bewertungsraster und dem Projektauftrag für Phase 4.
Daraus leitete ich ab, dass ich nicht nur einzelne erfolgreiche API-Aufrufe
zeigen sollte sondern auch die Service Logik mit Unit Tests, das HTTP-Verhalten
mit Spring-Boot-Tests und die REST-Endpunkte zusätzlich mit Postman prüfen sollte.

Ich wollte einfache, verständliche Tests und hatte Angst, dass ich durch zu viele
Testzeilen den Überblick verliere. Deshalb teilte ich die Tests in drei
ergänzende Ebenen auf.

| Ebene | Zweck | Ort |
|---|---|---|
| Unit Tests | Service- und Parserlogik isoliert prüfen, Repository-Aufrufe mocken | `src/test/java/.../service/` sowie Parser-Test für externe API-Daten |
| Spring-Boot-MVC-Tests | Controller-Routing, JSON-Validierung und HTTP-Status mit `MockMvc` prüfen | `src/test/java/.../controller/` |
| Postman | REST-Abläufe gegen ein laufendes Backend nachvollziehen | `postman/` |

Die automatisierten Tests können mit `.\mvnw.cmd test` ausgeführt werden. Der
Maven Wrapper verwendet die im Projekt vorgesehene Maven-Version, ohne dass
Maven separat installiert sein muss. Postman-Tests sind davon getrennt. Man
braucht ein laufendes Backend und eine erreichbare MariaDB-Datenbank. Die externen
Fahrplan- und Wetteraufrufe benötigen zusätzlich ihre jeweiligen Dienste.

## Verlauf und wichtige Entscheidungen

### 1. Erste Tests und reproduzierbare Testdaten

Zunächst habe ich einfache Unit- und Spring-Boot-Testbeispiele sowie eine erste

Bei Datumswerten fragte ich mich, ob feste Daten oder `now` besser geeignet
sind. Für Postman verwende ich ein dynamisches Datum. Der `Add Checklist`-
Request setzt `testDate` mit dem aktuellen Datum und nachfolgende Requests
verwenden dieselbe Variable. So funktioniert mein Ablauf auch an einem späteren
Testtag. Im Unit Test des Wetter-Parsers verwende ich dagegen absichtlich ein
festes Datum sowohl für die Fixture als auch für den Parseraufruf. Dadurch ist
der Test reproduzierbar und nicht von der Systemuhr abhängig.

### 2. Postman im Stil der bestehenden Collections

Ich wollte die Änderungen im Stil meiner vorhandenen Collections halten. Die Erfolgs-Requests
verwenden `{{baseUrl}}`. Ausserdem prüfen die Collections neben den Statuscodes
auch wichtige Antwortdaten.

Für die Checklist- und Setting-CRUD-Abläufe speichere ich die IDs aus den
Create-Antworten als Collection-Variablen. Die Requests zum Lesen,
Aktualisieren und Löschen verwenden diese IDs. Deshalb muss ich die
Erfolgs-Collection in der vorgegebenen Reihenfolge ausführen. In einer
separaten Fehlerfall-Collection (In Postman als "Bad Collection") prüfe ich unter anderem unbekannte IDs,
ungültige Datumsangaben, nicht unterstützte HTTP-Methoden und fehlerhafte
Eingaben.

Ich ergänzte ausserdem positive und negative Fahrplanfälle. Der Test des
externen Fahrplandiensts hängt davon ab, ob der Dienst erreichbar ist und wie
er antwortet. Meine automatisierten Fahrplan- und Wettertests rufen die
externen Dienste nicht auf, sondern prüfen die Verarbeitung von Beispiel-JSON.

### 3. Entwicklung der Wettervorhersage

Zuerst fragte ich, ob ich das Wetter spezifischer für Winterthur oder Zürich
anzeigen kann. Ich klärte dabei den Unterschied zwischen aktuellen Messwerten
(damals von der Station Kloten) und einer Vorhersage. Dort war ich zwischen den API Typen verwirrt. Ich entschied mich für
eine Vorhersage für den nächsten Tag mit Werten für Morgen, Mittag, Abend und
Nacht.

In meiner aktuellen Konfiguration stehen `WEATHER_LOCATION=Zürich,
Switzerland`. Der Controller fragt
Open-Meteo für diese Position ab und berechnet den morgigen Tag in der
Zeitzone `Europe/Zurich`. Die vier konfigurierten Zeitpunkte sind 7 Uhr, 12 Uhr,
18 Uhr und 22 Uhr. Ich stellte die Testdaten für den Fahrplan und die
Postman-Collections separat auf Zürich um. Dadurch änderte sich der
konfigurierte Wetterort nicht automatisch.

Ein Wettertest machte eine konkrete Annahme sichtbar. Meine JSON-Fixture muss
Zeitstempel für genau die angefragten Tageszeiten enthalten, weil der Parser
Werte anhand von Datum und Uhrzeit findet. Ich korrigierte die Fixture
entsprechend. Das Parserdatum halte ich im Unit Test fest und verwende es
sowohl für die Fixture als auch für den Parseraufruf. Für Checklist-CRUD in
Postman verwende ich dagegen `now`, damit der Ablauf auch an späteren Tagen
funktioniert.

### 4. Fachliche Testfälle für Checklists

Danach legte ich ausdrücklich fest, welche Verhaltenstests mir wichtig waren.
Mein Schwerpunkt lag auf den leicht zu übersehenden Fällen, insbesondere
Löschen und Wiederholungsvorlagen.

In `ChecklistServiceImplTest` prüfe ich die folgenden Regeln isoliert mit einem
Mock des Repositories.

- Löschfristen `null` und `1` sind gültig. `0` und negative Werte werden ohne
  Speichern abgewiesen.
- Eine einmalige Checklist wird anhand von `createdDate` beurteilt. Sie wird
  nicht vor Ablauf gelöscht. Der Grenztag wird explizit geprüft. Ein Eintrag
  ohne Löschfrist bleibt bestehen.
- Eine erzeugte Wiederholungskopie verwendet `dueDate` als Startdatum. Die
  Vorlage selbst wird nicht automatisch gelöscht.
- `getAllChecklists()` gibt nach der Bereinigung nur die verbleibenden Einträge
  zurück.
- `prepareTodaysChecklists()` erzeugt für einen passenden Wochentag eine
  heutige Kopie, überspringt unpassende Wochentage und erzeugt bei wiederholtem
  Aufruf keine Dublette. Task, Löschfrist, unerledigter Status und
  Vorlagenbezug werden geprüft.
- `getChecklistsForDate()` berücksichtigt passende einmalige Aufgaben und
  Kopien am `dueDate`, schliesst Vorlagen und Kopien für andere Tage aus und
  prüft auch eine leere Ergebnismenge.
- Erstellen, Aktualisieren, Statusänderung und Löschen prüfen erfolgreiche
  Änderungen sowie unbekannte IDs und ungültige Löschfristen.

### 5. Setting-Tests und HTTP-Verhalten

Mir fiel auf, dass für `SettingServiceImpl` und `SettingController` noch Tests
zu den CRUD-Funktionen fehlten. Mit den Service-Tests prüfe ich Speicherung,
Abruf mit Treffer und unbekannter ID, Rückgabe der Liste sowie Aktualisieren
und Löschen. Mit den MVC-Tests prüfe ich unter anderem.

- HTTP 201 beim Erstellen, den Antwortkörper und ungültige oder fehlende
  Pflichtfelder
- HTTP 200 beim Lesen und Aktualisieren, einschliesslich Übernahme der ID aus
  dem URL-Pfad
- HTTP 404 für unbekannte Datensätze und leere Settings-Liste entsprechend dem
  Controller-Verhalten
- HTTP 400 bei nicht numerischen IDs und fehlerhaftem JSON
- Status und Antwortkörper für erfolgreiche und fehlgeschlagene Löschungen.

### 6. Swagger-Abgleich und Validierungsprobleme

Beim Abgleich meiner Swagger-Dokumentation mit dem Code entdeckte ich mehrere
Unstimmigkeiten

- Swagger konnte Pflichtfelder anzeigen, die der Server nicht zuverlässig
  validierte. `@Schema` dokumentiert ein Modell, erzwingt aber keine
  Eingabeprüfung.
- Das fehlende Feld `completed` musste kontrolliert als HTTP 400 behandelt
  werden. Der Checklist-Status wurde deshalb nullable modelliert und mit
  `@NotNull` validiert. `task` ist ebenfalls Pflicht und nicht leer.
- Die drei Setting-Textfelder erhielten `@NotBlank`. Controller-Eingaben werden
  mit `@Valid` geprüft. Damit werden ungültige Eingaben vor dem Speichern
  abgewiesen.
- Ein Setting-Update prüfte zuvor nicht zuverlässig, ob die ID existiert. Der
  Service lädt jetzt den vorhandenen Datensatz, aktualisiert ihn und meldet
  eine unbekannte ID als 404. Auch das Löschen einer unbekannten ID liefert
  gezielt 404.
- Pauschale `catch (Exception)`-Blöcke im Setting-Controller konnten technische
  Fehler irreführend als „nicht gefunden“ ausgeben. Ich entfernte sie, damit
  unerwartete Fehler nicht als 404 maskiert werden. Swagger dokumentiert
  technische Server-/Datenbankfehler als 500.
- Ich passte die Modelle und Statuscodes in den Swagger-Antworten an die
  implementierten Regeln an.

Ausserdem benannte ich die Testdatei passend zur Klasse in
`ExternalAPIControllerTest.java` um. Bei `ForecastSlot` klärte ich, dass es
nur intern als Konfigurationsmodell verwendet wird und keine REST-Eingabe oder
-Ausgabe darstellt. Deshalb nehme ich es nicht als öffentliches Swagger-Schema
auf.

## Ergebnis des dokumentierten Teststands

Bei meinem zuletzt dokumentierten automatisierten Lauf bestanden **57 Tests
ohne Fehler**

| Bereich | Anzahl |
|---|---|
| Checklist-Service-Unit-Tests | 22 |
| Setting-Service-Unit-Tests | 8 |
| Externe Antwortverarbeitung (JSON-Parser) | 4 |
| Checklist-Controller-MVC-Tests | 7 |
| Setting-Controller-MVC-Tests | 15 |
| Vollständiger Spring-Anwendungskontext | 1 |
| **Gesamt** | **57** |

Die einzelnen Testfälle und meine Postman-Requests sind in
[`testing.md`](testing.md) aufgelistet. Ich habe hier nicht behauptet, dass ich
die manuellen Postman-Läufe gegen eine laufende Umgebung ausgeführt oder
Ergebnisse gespeichert habe.

## Mein Rückblick

Ich entwickelte meinen Testplan schrittweise. Zuerst leitete ich aus
Bewertungsraster und Projektauftrag passende Testebenen ab. Danach klärte ich
Testfehler und Datumsfragen, priorisierte fachliche Fälle und ergänzte die
Tests. Zum Schluss glich ich Swagger, Validierung und Fehlerstatuscodes mit
dem Code ab. Ich verteile die Tests bewusst auf mehrere Ebenen. Komplexe Regeln
wie Wiederholungen und automatisches Löschen teste ich isoliert im Service,
HTTP-Verträge im MVC-Test und vollständige API-Abläufe mit Postman.
