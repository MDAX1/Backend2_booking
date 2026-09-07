# Pensionat Bokningssystem

## Projektbeskrivning

Detta projekt är ett webbaserat bokningssystem utvecklat för ett mindre pensionat. Syftet med systemet är att effektivisera hanteringen av kunder, rum och bokningar genom en användarvänlig webbapplikation byggd med Spring Boot och Thymeleaf.

Systemet möjliggör registrering och administration av kunder samt skapande, uppdatering och avbokning av bokningar. All data lagras permanent i en relationsdatabas som byggs enligt Code First-principen med hjälp av Hibernate och JPA.

Projektet är utvecklat enligt en tydlig lagerarkitektur där ansvar delas upp mellan Controllers, Services, Repositories och DTO-objekt för att säkerställa struktur, underhållbarhet och skalbarhet.

---

# Funktionalitet

## Kundhantering
- Registrera nya kunder
- Uppdatera kundinformation
- Visa kundlista
- Hantera validering av kunddata

## Rumshantering
- Hantera olika rumstyper
    - Enkelrum
    - Dubbelrum
- Stöd för extrasängar i utvalda dubbelrum
- Visa tillgängliga rum

## Bokningshantering
- Skapa bokningar
- Uppdatera befintliga bokningar
- Avboka bokningar
- Koppla bokning till kund och rum
- Hantera bokningar inom specifika datumintervall

## Tillgänglighet och sökning
- Sök lediga rum baserat på:
    - Datum
    - Datumintervall
    - Antal personer
- Förhindra dubbelbokningar genom kontroll av överlappande bokningar

## Validering och felhantering
- Validering av inmatad data med Bean Validation
- Tydlig återkoppling vid:
    - Lyckade bokningar
    - Felaktiga inmatningar
    - Ogiltiga datumintervall

## Tester
- Enhetstester för affärslogik och funktionalitet
- Testning av bokningsregler och valideringar

---

# Teknologier

Projektet är byggt med följande teknologier och ramverk:

- Java
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- Thymeleaf
- MySQL / H2 Database
- Maven
- JUnit
- HTML/CSS

---

# Arkitektur

Projektet följer en lagerbaserad arkitektur:

## Controller Layer
Ansvarar för:
- Hantering av HTTP-requests
- Routing mellan sidor
- Kommunikation med Thymeleaf-vyer

## Service Layer
Ansvarar för:
- Affärslogik
- Bokningsregler
- Validering av bokningsflöden
- Kontroll av dubbelbokningar

## Repository Layer
Ansvarar för:
- Databaskommunikation
- CRUD-operationer
- JPA/Hibernate-integration

## DTO Layer
Ansvarar för:
- Dataöverföring mellan lager
- Separation mellan entiteter och presentation

---

# Databas

Databasen byggs enligt Code First-principen där tabeller genereras automatiskt från entitetsklasser via Hibernate/JPA.

Exempel på entiteter:
- Customer
- Room
- Booking

Relationer:
- En kund kan ha flera bokningar
- Ett rum kan ha flera bokningar över tid
- En bokning kopplas till exakt ett rum och en kund

---

# Installation

## Klona projektet

```bash
git clone https://github.com/ditt-användarnamn/pensionat-booking-system.git