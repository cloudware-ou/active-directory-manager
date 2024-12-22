FROM mcr.microsoft.com/powershell:7.4-debian-12
RUN pwsh -Command 'Install-Module -Name PSWSMan -Confirm:$False -Force'
RUN pwsh -Command 'Install-WSMan'
#RUN apt-get update && apt-get install -y wget build-essential
RUN apt-get update && apt-get install -y wget libsodium-dev

#RUN wget -O libsodium.tar.gz https://download.libsodium.org/libsodium/releases/libsodium-1.0.20.tar.gz
#RUN tar -zxvf libsodium.tar.gz
#WORKDIR /libsodium-1.0.20
#RUN ./configure
#RUN make && make check
#RUN make install
#WORKDIR /

RUN wget -O mela.nupkg https://www.nuget.org/api/v2/package/Microsoft.Extensions.Logging.Abstractions/8.0.2
RUN wget -O npgsql.nupkg https://www.nuget.org/api/v2/package/Npgsql/8.0.5
RUN wget -O sodium.core.nupkg https://www.nuget.org/api/v2/package/Sodium.Core/1.3.3
#RUN wget -O libsodium.nupkg https://www.nuget.org/api/v2/package/libsodium/1.0.20.1
#RUN pwsh -Command 'Expand-Archive -Path "./libsodium.nupkg" -DestinationPath "./libsodium"'
RUN pwsh -Command 'Expand-Archive -Path "./sodium.core.nupkg" -DestinationPath "./sodium.core"'
RUN pwsh -Command 'Expand-Archive -Path "./npgsql.nupkg" -DestinationPath "./npgsql"'
RUN pwsh -Command 'Expand-Archive -Path "./mela.nupkg" -DestinationPath "./microsoft.extentions.logging.abstractions/"'
COPY Main.ps1 .
ENTRYPOINT ["pwsh"]
CMD ["./Main.ps1"]