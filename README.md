# Pensionat Bokningssystem

## JWT för API-anrop

Alla `/api/**` kräver `Authorization: Bearer <token>`, inklusive
`GET /api/bookings/count?customerId=42&status=ACTIVE`. Även befintliga POST-endpoints som
skapar, ändrar och avbokar bokningar eller ändrar rum kräver token (`/bookings/**` och
`/rooms/**`). Sökningen `POST /bookings/search` är publik eftersom den bara läser data.
Hämta token från kundtjänstens
`POST /api/auth/login` med JSON-fälten `username` och `password`; svaret innehåller `token`.

Bokningstjänsten validerar HS256-signaturen, issuer `pensionat-customer-service`, audience
`pensionat` och giltighetstiden. Token måste ha `exp`; saknad, utgången eller ogiltig token
ger HTTP 401. Vid kunduppslag vidarebefordras anropets validerade token till kundtjänsten.
Token i URL-parametrar accepteras inte, och autentisering sparas inte i en session.

Sätt `JWT_SECRET` till **samma Base64-kodade nyckel som kundtjänsten** (minst 32 byte).
Tjänsten vägrar starta om nyckeln saknas eller är ogiltig. Ingen nyckel ska checkas in.

```bash
# JWT_SECRET ska redan vara satt till kundtjänstens nyckel.
./mvnw spring-boot:run

# TOKEN är token från kundtjänstens inloggningssvar.
curl -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8081/api/bookings/count?customerId=42&status=ACTIVE'
```

För Docker Compose ska `booking-service.environment.JWT_SECRET` sättas till `${JWT_SECRET}`.
För Kubernetes ska booking-containern läsa `JWT_SECRET` från samma Secret som kundtjänsten
(`pensionat-secrets`, nyckeln `jwt-secret`). Dessa konfigurationer finns i kundtjänstens repo.

Det här stödet gäller Bearer-token i anrop. Ingen inloggningssida läggs till.
GET-anrop till Thymeleaf-sidorna (`/`, `/rooms/**`, `/bookings/**`), statiska resurser,
Swagger och hälsokontroller behåller sin publika åtkomst. Vanliga webbläsarformulär skickar
ingen Bearer-token och får därför 401 när de ändrar data. Använd Postman/curl med headern;
stöd för autentisering i webbläsaren är ett separat inloggningsflöde.

Kör testerna med Java 21: `./mvnw test`. JWT-testerna använder signerade testtoken och
kontrollerar både godkända och nekade anrop; kundklientens tokenvidarebefordran testas separat.

## Docker och Railway

`Dockerfile` bygger Java 21-applikationen och dess Thymeleaf-frontend. `railway.json`
väljer Dockerfile-byggaren och hälsokontrollen `/actuator/health/readiness`.

```bash
docker build -t booking-service .
```

Deploya repot som en egen Railway-tjänst med egen PostgreSQL-databas på bokningsansvariges
konto. Kund- och notifieringstjänsten finns på Joakims konto i ett annat projekt. Sätt `PORT=8081`,
`SPRING_DATASOURCE_URL` till databasens JDBC-adress, `SPRING_DATASOURCE_USERNAME`,
`SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET` och `CUSTOMER_SERVICE_URL` till kundtjänstens
publika HTTPS-basadress (utan `/api` eller portnummer). Generera en publik domän med target
port 8081 och skicka basadressen till Joakim, som sätter den som `BOOKING_SERVICE_URL`.
`JWT_SECRET` måste vara exakt samma som kundtjänstens; få värdet från Joakim innan deployment.

Kundtjänsten är deployad på Railway. Använd följande basadress:

```dotenv
CUSTOMER_SERVICE_URL=https://customer-service-production-bbb4.up.railway.app
```

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
git clone https://github.com/MDAX1/Backend2_booking.git
```
