<Project Sdk="Microsoft.NET.Sdk.Web">

  <PropertyGroup>
    <TargetFramework>net461</TargetFramework>
    <AssemblyName>Profile.Api</AssemblyName>
    <RootNamespace>Profile.Api</RootNamespace>
    <GenerateAssemblyInfo>false</GenerateAssemblyInfo>
  </PropertyGroup>

  <!-- NuGet Package References -->
  <ItemGroup>
    <PackageReference Include="AutoMapper" Version="5.1.1" />
    <PackageReference Include="Consul" Version="0.7.2.1" />
    <PackageReference Include="EntityFramework" Version="6.1.3" />
    <PackageReference Include="FluentValidation" Version="6.2.1.0" />
    <PackageReference Include="Microsoft.AspNetCore" Version="2.0.2" />
    <PackageReference Include="Microsoft.AspNet.WebApi.Client" Version="5.2.3" />
    <PackageReference Include="Microsoft.AspNet.WebApi.Core" Version="5.2.3" />
    <PackageReference Include="Microsoft.AspNet.WebApi.WebHost" Version="5.2.3" />
    <PackageReference Include="Microsoft.Owin" Version="4.2.2" />
    <PackageReference Include="Microsoft.Owin.Host.SystemWeb" Version="4.2.2" />
    <PackageReference Include="Microsoft.Owin.Startup" Version="4.2.2" />
    <PackageReference Include="Owin" Version="1.0" />
    <PackageReference Include="SimpleInjector" Version="5.3.0" />
    <PackageReference Include="SimpleInjector.Integration.Owin" Version="5.3.0" />
    <PackageReference Include="Newtonsoft.Json" Version="10.0.3" />
    <PackageReference Include="RabbitMQ.Client" Version="4.1.1" />
    <PackageReference Include="Serilog" Version="2.5.0" />
    <PackageReference Include="StackExchange.Redis" Version="1.2.0" />
    <PackageReference Include="System.Collections.Immutable" Version="1.4.0" />
    <PackageReference Include="System.Text.Encodings.Web" Version="4.4.0" />
    <PackageReference Include="System.Runtime.CompilerServices.Unsafe" Version="4.4.0" />
    <PackageReference Include="System.Security.Principal.Windows" Version="4.4.0" />
    <PackageReference Include="System.Threading.Tasks.Extensions" Version="4.4.0" />
    <PackageReference Include="Toolkit" Version="17.4.2" />
  </ItemGroup>

  <!-- Content and None Includes -->
  <ItemGroup>
    <Content Include="Web.config">
      <CopyToOutputDirectory>Always</CopyToOutputDirectory>
    </Content>
    <None Include="Readme Field Validation.txt" />
    <None Include="Readme Fluent Validation.txt" />
  </ItemGroup>

</Project>
