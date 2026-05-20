---
id: TASK-20
title: Migrate from JitPack to Maven Central
status: Done
assignee: []
created_date: '2026-05-20 19:45'
updated_date: '2026-05-20 20:19'
labels:
  - publishing
dependencies:
  - TASK-18
references:
  - decision-5
  - TASK-18
priority: high
ordinal: 20000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Switch primary distribution from JitPack to Maven Central under new group ID io.github.lenucksi.

JitPack war als schnelle Lösung für SieveEditor gedacht, ist aber kein echtes Maven-Repository und hat Downtime-Risiken. Maven Central ist der Standard für Java-Bibliotheken — automatische Dependency-Resolution ohne extra Repository-Eintrag.

Weil die Domain fluffypeople.com nicht dem Fork-Betreiber gehört, muss die groupId auf io.github.lenucksi wechseln.

Neuer Publishing-Weg (seit 2024): Zentrales Portal unter central.sonatype.com statt OSSRH JIRA. GitHub-Login erzeugt automatisch verifizierten Namespace io.github.<username>. Kein JIRA-Ticket mehr nötig. Neues Maven-Plugin: central-publishing-maven-plugin (ersetzt nexus-staging-maven-plugin). Authentifizierung via Portal Token (generiert auf central.sonatype.com/usertoken).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria

<!-- AC:BEGIN -->

- [ ] #1 GitHub-Login auf central.sonatype.com funktioniert, Namespace io.github.lenucksi ist automatisch verifiziert
- [ ] #2 Portal Token ist generiert und in GitHub Secrets hinterlegt (CENTRAL_TOKEN_USER, CENTRAL_TOKEN_PASS)
- [ ] #3 GPG-Key ist generiert und auf keyserver.ubuntu.com publiziert
- [ ] #4 pom.xml: groupId, central-publishing-maven-plugin, GPG-Signing, Javadoc/Sources-JARs sind konfiguriert
- [ ] #5 release.yml deployed erfolgreich auf Maven Central (snapshot + release via Portal)
- [ ] #6 Dokumentation ist aktualisiert (README, JITPACK.md)
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->

## Phase 1 - Voraussetzungen (Portal & Credentials)

- [ ] Bei <https://central.sonatype.com> mit GitHub anmelden
- [ ] Prüfen ob Namespace io.github.lenucksi automatisch verifiziert ist
- [ ] Portal Token generieren: <https://central.sonatype.com/usertoken>
  - Token hat format: username:password (base64 encodiert für Basic Auth)
  - Kann nicht nach Schließen des Modals wieder abgerufen werden → sofort speichern
- [ ] GitHub Secrets setzen:
  - CENTRAL_TOKEN_USER (Token-User)
  - CENTRAL_TOKEN_PASS (Token-Pass)
  - GPG_PRIVATE_KEY (Inhalt von gpg --armor --export-secret-key <id>)
  - GPG_PASSPHRASE
- [ ] GPG-Key generieren: gpg --full-generate-key
- [ ] GPG-Public-Key publizieren: gpg --keyserver keyserver.ubuntu.com --send-key <id>

## Phase 2 - pom.xml

- [ ] groupId ändern: com.fluffypeople -> io.github.lenucksi
- [ ] nexus-staging-maven-plugin ENTFERNEN (nicht mehr nötig)
- [ ] central-publishing-maven-plugin HINZUFÜGEN (org.sonatype.central)
- [ ] GPG-Signing via maven-gpg-plugin konfigurieren
- [ ] Javadoc + Sources JARs sind bereits konfiguriert (prüfen)
- [ ] distributionManagement anpassen auf Portal-URLs:
  - releases: <https://central.sonatype.com/repository/>...
  - siehe Plugin-Doku für korrekte URL
- [ ] settings.xml-Vorlage für Portal-Token erstellen (local + CI)

## Phase 3 - CI/CD (release.yml)

- [ ] s4u/maven-settings-action erweitern für Portal-Token-Credentials
- [ ] Release-Step: mvn deploy (nutzt central-publishing-maven-plugin)
  - autoPublish=true für CI
  - waitUntil=published für CI-Sicherheit
- [ ] GitHub Packages parallel behalten ODER durch Maven Central ersetzen
- [ ] Snapshot-Publishing prüfen (central-publishing-maven-plugin unterstützt SNAPSHOT seit v0.7.0)

## Phase 4 - Dokumentation

- [ ] README.md: Maven Central Koordinaten aktualisieren (io.github.lenucksi)
- [ ] JITPACK.md: JitPack als deprecated markieren, Maven Central als primär empfehlen
- [ ] release.yml: usage.md-Template für Release-Notes aktualisieren

## Phase 5 - Test & Go Live

- [ ] mvn clean deploy (lokal testen mit settings.xml)
- [ ] Tag + Release via release-please durchführen
- [ ] Auf Maven Central verifizieren: <https://central.sonatype.com/publishing>
- [ ] Decision-5 aktualisieren
- [ ] TASK-18 (JitPack) ggf. mit Label 'deprecated' versehen
<!-- SECTION:PLAN:END -->

## Definition of Done

<!-- DOD:BEGIN -->

- [ ] #1 Erstes Release (z.B. 0.3.14) ist auf Maven Central sichtbar unter io.github.lenucksi:managesievej
- [ ] #2 Release-Workflow published sowohl auf Maven Central als auch (optional) auf GitHub Packages
- [ ] #3 JitPack ist in README und JITPACK.md als deprecated markiert
- [ ] #4 Decision-5 ist aktualisiert (JitPack+GitHub Packages -> Maven Central+GitHub Packages)
<!-- DOD:END -->
