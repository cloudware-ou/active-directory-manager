FROM mcr.microsoft.com/powershell:7.4-alpine-3.20

RUN apk add --no-cache wget libsodium-dev

WORKDIR /app

RUN wget -O mela.nupkg https://www.nuget.org/api/v2/package/Microsoft.Extensions.Logging.Abstractions/9.0.0
RUN wget -O npgsql.nupkg https://www.nuget.org/api/v2/package/Npgsql/9.0.2
RUN wget -O sodium.core.nupkg https://www.nuget.org/api/v2/package/Sodium.Core/1.3.5

RUN pwsh -Command 'Expand-Archive -Path "./sodium.core.nupkg" -DestinationPath "./sodium.core"'
RUN pwsh -Command 'Expand-Archive -Path "./npgsql.nupkg" -DestinationPath "./npgsql"'
RUN pwsh -Command 'Expand-Archive -Path "./mela.nupkg" -DestinationPath "./microsoft.extentions.logging.abstractions/"'

WORKDIR /app/src

COPY ./src .

ENTRYPOINT ["pwsh"]
CMD ["./Main.ps1"]