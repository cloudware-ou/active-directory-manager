FROM mcr.microsoft.com/powershell:7.4-debian-12

RUN apt-get update && apt-get install -y wget libsodium-dev

WORKDIR /app

RUN wget -O mela.nupkg https://www.nuget.org/api/v2/package/Microsoft.Extensions.Logging.Abstractions/8.0.2
RUN wget -O npgsql.nupkg https://www.nuget.org/api/v2/package/Npgsql/8.0.5
RUN wget -O sodium.core.nupkg https://www.nuget.org/api/v2/package/Sodium.Core/1.3.3

RUN pwsh -Command 'Expand-Archive -Path "./sodium.core.nupkg" -DestinationPath "./sodium.core"'
RUN pwsh -Command 'Expand-Archive -Path "./npgsql.nupkg" -DestinationPath "./npgsql"'
RUN pwsh -Command 'Expand-Archive -Path "./mela.nupkg" -DestinationPath "./microsoft.extentions.logging.abstractions/"'
COPY Main.ps1 .
ENTRYPOINT ["pwsh"]
CMD ["./Main.ps1"]