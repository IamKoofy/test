<Project Sdk="Microsoft.NET.Sdk">

  <PropertyGroup>
    <OutputType>Exe</OutputType>
    <TargetFramework>net6.0</TargetFramework>
    <RootNamespace>Profile.Sync.Organizer</RootNamespace>
    <AssemblyName>Profile.Sync.Organizer</AssemblyName>
  </PropertyGroup>

  <ItemGroup>
    <PackageReference Include="Consul" Version="0.7.2.1" />
    <PackageReference Include="EntityFramework" Version="6.1.3" />
    <PackageReference Include="EventStore.ClientAPI" Version="3.9.4" />
    <PackageReference Include="Newtonsoft.Json" Version="9.0.1" />
    <PackageReference Include="RabbitMQ.Client" Version="4.1.1" />
    <PackageReference Include="Serilog" Version="2.3.0" />
    <PackageReference Include="Serilog.Formatting.Compact" Version="1.0.0" />
    <PackageReference Include="SimpleInjector" Version="3.2.3" />
    <PackageReference Include="StackExchange.Redis" Version="1.2.0" />
    <PackageReference Include="Microsoft.AspNet.WebApi.Client" Version="5.2.3" />
    <PackageReference Include="Topshelf" Version="4.0.3" />
  </ItemGroup>

</Project>
