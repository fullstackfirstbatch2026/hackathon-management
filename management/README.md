# Management API

## Local database configuration

The application reads its MySQL password from `DB_PASSWORD`. Set it in the environment used to launch Spring Boot (for example, the IntelliJ run configuration or the same PowerShell session). `DB_USERNAME` is optional and defaults to `root` for the existing local setup.

PowerShell example for a one-session launch:

```powershell
$env:DB_PASSWORD = Read-Host 'MySQL password'
.\mvnw.cmd spring-boot:run
```

Do not commit the password or put it in `application.properties`.
