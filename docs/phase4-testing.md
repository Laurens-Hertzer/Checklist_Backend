# Phase 4 - Backend Testing

## Ziel und Vorgehen der Tests

Geprüft werden die zentrale ChecklistService-Logik und die HTTP-Kommunikation der
REST-API. Unit Tests isolieren den Service mit einem Mock des Repositories.
Der Spring-Boot-MVC-Test startet den Controller in einem Spring-Testkontext und
sendet HTTP-Anfragen mit `MockMvc`. So werden Routing, JSON und HTTP-Status
geprüft, ohne eine echte Datenbank mit den Datensätzen zu verändern.

| Testart | Was wird geprüft? | Beispiel |
|---|---|---|
| Unit Test | Service-Logik und Repository-Aufrufe | Erledigungsstatus speichern; unbekannte ID und ungültige Löschfrist |
| Spring-Boot-Test | HTTP-Endpunkt im Spring-MVC-Kontext | `POST` liefert 201; fehlerhafte ID/fehlendes Datum liefern 400; unbekannte Checklist liefert 404 |
| API-Test mit Postman | REST-Endpunkte gegen das laufende Backend | Erfolgsfälle in `postman/Checklist_Backend.postman_collection.json`, Fehlerfälle in `postman/Checklist Backend Bad Tests.postman_collection.json`; Fahrplan: 200 mit Verbindungsdaten bzw. 404 bei ungültigen Haltestellen |

Die Fehlerfälle sind wichtig, weil die API nicht nur bei gültigen Eingaben,
sondern auch mit passenden HTTP-Statuscodes auf ungültige Parameter und nicht
vorhandene Ressourcen reagieren soll.

## Ausführen

1. Tests automatisiert starten: `.\mvnw.cmd test`
2. Für Postman das Backend mit einer erreichbaren, konfigurierten Datenbank
   starten.
3. Beide Collections importieren. In beiden Collections die Variable `baseUrl`
   auf die Backend-Adresse setzen (standardmäßig `http://localhost:8080`).
4. Die Requests ausführen und in Postman die tatsächlichen Statuscodes und
   Testergebnisse kontrollieren. Die Bad-Tests-Collection sollte nur gegen
   eine Test-/Entwicklungsumgebung ausgeführt werden.

## Tests

### Unit Tests

`ChecklistServiceImplTest` testet die Business-Logik isoliert mit einem Mock
des Repositories. Die 22 Tests sind nach Service-Funktion gruppiert:

| Funktion und Testfälle | Was wird geprüft? |
|---|---|
| `createChecklist_acceptsNullDeletionPeriod`, `createChecklist_acceptsDeletionPeriodOfOneDay`, `createChecklist_rejectsZeroOrNegativeDeletionPeriodWithoutSaving` | Prüft akzeptierte Löschfristen (`null`, `1`) und lehnt `0` sowie negative Werte ab, ohne zu speichern. |
| `createChecklist_createsTodaysRecurringCopyForMatchingWeekday` | Prüft, dass eine Vorlage bei passendem Wochentag eine heutige, unerledigte Kopie mit Task, Löschfrist und Vorlagen-ID erzeugt. |
| `getChecklistById_returnsChecklistWhenFound`, `getChecklistById_returnsNotFoundWhenMissing` | Prüft Abruf einer vorhandenen Checklist und 404 bei unbekannter ID. |
| `getAllChecklists_deletesExpiredOneOffAndReturnsRemainingChecklists`, `getAllChecklists_usesCreatedDateAndKeepsChecklistUntilBoundaryDay`, `getAllChecklists_usesDueDateForGeneratedCopyAndNeverDeletesTemplate` | Prüft Löschung abgelaufener Einzelaufgaben am Grenztag, Erhalt noch nicht abgelaufener Aufgaben und Aufgaben ohne Löschfrist, Verwendung von `createdDate` bzw. `dueDate` sowie Erhalt der Wiederholungsvorlage. Prüft auch, dass nur verbleibende Einträge zurückgegeben werden. |
| `getChecklistsForDate_returnsOneOffAndMatchingCopiesButNotTemplates`, `getChecklistsForDate_returnsEmptyListWhenNothingMatches` | Prüft den Datumsfilter für einmalige Aufgaben und erzeugte Kopien, schließt Vorlagen und Kopien mit anderem `dueDate` aus und prüft eine leere Ergebnismenge. |
| `setChecklistCompletion_updatesAndSavesChecklist`, `setChecklistCompletion_throwsNotFoundWhenChecklistDoesNotExist` | Prüft Statusänderung und Speichern sowie 404 bei unbekannter ID. |
| `prepareTodaysChecklists_createsCopyForTodayAndCopiesTemplateFields`, `prepareTodaysChecklists_skipsTemplateForAnotherWeekday`, `prepareTodaysChecklists_doesNotCreateDuplicateWhenCalledAgain`, `prepareTodaysChecklists_deletesExpiredChecklistsBeforeCreatingCopies` | Prüft heutige Wiederholung, falschen Wochentag, Idempotenz bei mehrmaligem Aufruf und automatische Löschung abgelaufener Einträge beim Start-/Tageslauf. |
| `updateChecklist_updatesFieldsAndCreatesTodaysRecurringCopy`, `updateChecklist_rejectsInvalidDeletionPeriodWithoutSaving`, `updateChecklist_returnsNotFoundForUnknownId` | Prüft Aktualisierung aller Checklist-Felder, Anlage einer passenden heutigen Kopie sowie ungültige Löschfrist und unbekannte ID. |
| `deleteChecklist_deletesExistingChecklist`, `deleteChecklist_returnsNotFoundForUnknownId` | Prüft Löschen einer vorhandenen Checklist und 404 bei unbekannter ID. |

`SettingServiceImplTest` deckt alle Funktionen des Setting-Services ab:

| Test | Beschreibung |
|---|---|
| `createSetting_savesSetting` | Prüft, dass ein neues Setting gespeichert wird. |
| `getSettingById_returnsExistingSetting`, `getSettingById_returnsNotFoundForUnknownId` | Prüft Abruf und 404 bei einer unbekannten ID. |
| `getAllSettings_returnsRepositoryResults` | Prüft, dass die Settings-Liste aus dem Repository zurückgegeben wird. |
| `updateSetting_updatesExistingSetting` | Prüft, dass ein bestehendes Setting aktualisiert und gespeichert wird. |
| `updateSetting_returnsNotFoundForUnknownIdWithoutSaving` | Prüft 404 für unbekannte IDs und dass nichts gespeichert wird. |
| `deleteSetting_deletesExistingSetting` | Prüft, dass ein bestehendes Setting gelöscht wird. |
| `deleteSetting_returnsNotFoundForUnknownIdWithoutDeleting` | Prüft 404 für unbekannte IDs und dass kein Löschaufruf erfolgt. |

In `ExternalAPIControllerTest` werden die Antworten der externen APIs direkt
aus Test-JSON verarbeitet, ohne echte HTTP-Aufrufe zu machen:

| Test | Beschreibung |
|---|---|
| `parsesTrainConnection` | Prüft, ob Abfahrtsbahnhof, Zielbahnhof, Abfahrtszeit und Verspätung korrekt aus einer Fahrplan-Antwort gelesen werden. |
| `rejectsTrainResponseWithoutConnections` | Prüft, dass eine leere Fahrplan-Antwort als 404 behandelt wird. |
| `parsesForecastIntoFourDayPeriods` | Prüft, ob die vier Wetter-Zeitabschnitte mit Datum und Wetterwerten aus der Antwort erzeugt werden. |
| `rejectsForecastWithMissingHourlyData` | Prüft, dass fehlende Wetterdaten als 502 behandelt werden. |

### Spring-Boot-Tests

`ChecklistControllerTest` verwendet `@WebMvcTest` und `MockMvc`, um
HTTP-Anfragen an den Checklist-Controller im Spring-MVC-Testkontext zu senden:

| Test | Beschreibung |
|---|---|
| `createChecklist_returnsCreatedResponseAndJson` | Prüft, dass gültiges JSON eine Checklist anlegt und HTTP 201 sowie die erwarteten JSON-Felder zurückgibt. |
| `createChecklist_returnsBadRequestWhenRequiredFieldsAreMissing` | Prüft, dass `completed` als Pflichtfeld validiert wird und ein fehlender Wert HTTP 400 auslöst. |
| `createChecklist_returnsBadRequestWhenTaskIsBlank` | Prüft, dass ein leerer Task HTTP 400 auslöst. |
| `updateChecklist_returnsBadRequestWhenTaskIsBlank` | Prüft, dass auch beim Aktualisieren ein leerer Task HTTP 400 auslöst. |
| `getChecklist_returnsNotFoundForUnknownId` | Prüft, dass eine nicht vorhandene Checklist als HTTP 404 beantwortet wird. |
| `getChecklist_returnsBadRequestForNonNumericId` | Prüft, dass eine nicht numerische ID HTTP 400 auslöst. |
| `getChecklistsByDate_returnsBadRequestWhenDateIsMissing` | Prüft, dass der fehlende Datumsparameter HTTP 400 auslöst. |

`SettingControllerTest` prüft Validierung und HTTP-Fehlerverhalten:

| Test | Was wird geprüft? |
|---|---|
| `getAllSettings_returnsNotFoundWhenListIsEmpty` | Prüft, dass eine leere Liste HTTP 404 liefert. |
| `getSettingById_returnsSetting`, `getSettingById_returnsNotFoundWhenMissing`, `getSettingById_returnsBadRequestForNonNumericId` | Prüft erfolgreiches Lesen sowie die Statuscodes 404 und 400. |
| `createSetting_returnsCreatedSetting`, `createSetting_returnsBadRequestWhenRequiredFieldsAreMissing`, `createSetting_returnsBadRequestWhenRequiredFieldIsBlank`, `createSetting_returnsBadRequestForMalformedJson` | Prüft HTTP 201 für gültige Daten und HTTP 400 für fehlende/leere Pflichtfelder oder ungültiges JSON. |
| `updateSetting_usesPathIdAndReturnsSuccessMessage`, `updateSetting_returnsBadRequestWhenRequiredFieldIsMissing`, `updateSetting_returnsNotFoundWhenSettingDoesNotExist`, `updateSetting_returnsBadRequestForNonNumericId` | Prüft, dass die URL-ID verwendet wird, Pflichtfelder validiert werden und die Statuscodes 200, 400 und 404 stimmen. |
| `deleteSetting_returnsSuccessMessage`, `deleteSetting_returnsNotFoundWhenSettingDoesNotExist`, `deleteSetting_returnsBadRequestForNonNumericId` | Prüft erfolgreiche Löschung sowie 404 und 400 für Fehlerfälle. |

`ChecklistBackendApplicationTests.contextLoads` startet mit `@SpringBootTest`
den gesamten Spring-Anwendungskontext und prüft, dass dieser hochfahren kann.
Zusammen sind es 57 automatisierte Tests: 22 Checklist-Service-Unit-Tests,
8 Setting-Service-Unit-Tests, 4 Tests für die externe Antwortverarbeitung,
7 Checklist-Controller-Tests, 15 Setting-Controller-Tests und 1
Anwendungskontext-Test. CRUD-Abläufe werden zusätzlich mit Postman gegen das
laufende Backend geprüft.

Ungültige Eingaben werden durch Bean Validation mit HTTP 400 abgewiesen, bevor
ein Datenbankzugriff erfolgt. Unbekannte Setting-IDs liefern gezielt HTTP 404.
Die Controller fangen unerwartete Datenbank- oder Serverfehler nicht mehr
pauschal ab; diese bleiben HTTP 500, statt fälschlich als „nicht gefunden“
ausgegeben zu werden.

### Postman-API-Tests

Die Erfolgs-Collection `Checklist_Backend` enthält geordnete CRUD-Abläufe.
`Add Checklist` bzw. `Add Setting` speichern die erzeugte ID als
Collection-Variable; nachfolgende Requests verwenden diese ID. Die Checklist
wird mit dem aktuellen Datum erstellt, aktualisiert, gelesen und anschliessend
gelöscht. Bei Einstellungen wird das gleiche CRUD-Vorgehen ausgeführt. Die
Requests enthalten Postman-Prüfungen für erwartete Statuscodes und wichtige
Antwortdaten:

| Request | Beschreibung |
|---|---|
| `Get all Checklists` | Ruft alle Checklists ab. |
| `Get Checklist by Id` | Prüft, dass die zuvor erstellte und aktualisierte Checklist über ihre ID gelesen wird. |
| `Get Checklist by date` | Prüft, dass die Checklist für ihr Fälligkeitsdatum in der Antwort enthalten ist. |
| `Add Checklist` | Prüft HTTP 201 und speichert die neue ID für die folgenden Requests. |
| `Update a Checklist by Id` | Ändert Task, Status und Fälligkeitsdatum; der anschliessende Abruf prüft die Änderung. |
| `Update a Checklist completion by Id` | Setzt den Erledigungsstatus und prüft den Wert in der Antwort sowie im anschliessenden Abruf. |
| `Delete a checklist by Id` | Prüft die erfolgreiche Löschantwort für die erzeugte ID. |
| `Get all Settings` | Prüft HTTP 200 und eine JSON-Liste, nachdem eine Einstellung angelegt wurde. |
| `Get Setting by Id` | Prüft, dass die aktualisierte Einstellung über ihre erzeugte ID gelesen wird. |
| `Add Setting` | Prüft HTTP 201 und speichert die neue ID für die folgenden Requests. |
| `Update Setting by Id` | Ändert eine Einstellung und prüft die Änderung beim anschliessenden Abruf. |
| `Delete a Setting by Id` | Prüft erfolgreichen Status und Antworttext beim Löschen der erzeugten Einstellung. |
| `Train` | Fragt die nächste Verbindung von Zürich nach Winterthur ab und prüft HTTP 200 sowie Verbindungsdaten. Benötigt den externen Fahrplandienst. |
| `Weather` | Ruft die Wettervorhersage ab und prüft HTTP 200 sowie vorhandene Perioden. Benötigt den externen Wetterdienst. |

Die Erfolgs-Collection im Collection Runner in der vorhandenen Reihenfolge
ausführen, damit die erzeugten IDs für die Folge-Requests gesetzt sind.

Die Fehlerfall-Collection `Checklist Backend Bad Tests` enthält folgende Requests:

| Request | Beschreibung |
|---|---|
| `Bad Get Checklist by Id` | Fragt eine nicht vorhandene Checklist-ID ab. |
| `Bad Checklist Aufruf` | Sendet eine nicht unterstützte PATCH-Anfrage an die Checklist-Collection. |
| `Bad Get all Checklists by date` | Übergibt ein Datum im falschen Format. |
| `Bad Put Checklist by Id` | Ändert den Erledigungsstatus einer nicht vorhandenen Checklist. |
| `Bad Add Checklist by Delete After Days` | Versucht, eine Checklist mit einer Löschfrist von 0 Tagen anzulegen. |
| `Bad Add Checklist Missing Required Fields` | Lässt das Pflichtfeld `completed` weg und erwartet HTTP 400. |
| `Bad Update Checklist by Delete After Days` | Versucht, eine Checklist mit einer Löschfrist von 0 Tagen zu aktualisieren. |
| `Bad Delete Checklist` | Versucht, eine nicht vorhandene Checklist zu löschen. |
| `Bad Get Setting by Id` | Fragt eine nicht vorhandene Einstellungs-ID ab. |
| `Bad Setting Aufruf` | Sendet eine nicht unterstützte PATCH-Anfrage an die Setting-Collection. |
| `Bad Add Setting` | Sendet ungültiges JSON beim Erstellen einer Einstellung. |
| `Bad Add Setting Missing Required Fields` | Lässt erforderliche Setting-Felder weg und erwartet HTTP 400. |
| `Bad Number Update Setting` | Verwendet beim Aktualisieren eine nicht numerische Einstellungs-ID. |
| `Bad Number Delete ID` | Verwendet beim Löschen eine nicht numerische Einstellungs-ID. |
| `Train Bad Station` | Fragt mit unbekannten Haltestellen eine Verbindung ab und prüft HTTP 404, wenn der externe Dienst erreichbar ist und keine Verbindung findet. |
| `Weather unsupported Method` | Sendet eine nicht unterstützte POST-Anfrage an den Wetter-Endpunkt. |
