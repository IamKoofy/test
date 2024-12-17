<Project Sdk="Microsoft.NET.Sdk.Web">

  <PropertyGroup>
    <TargetFramework>net461</TargetFramework>
    <AssemblyName>Profile.Api</AssemblyName>
    <RootNamespace>Profile.Api</RootNamespace>
  </PropertyGroup>

  <!-- NuGet Package References -->
  <ItemGroup>
    <PackageReference Include="AutoMapper" Version="5.1.1" />
    <PackageReference Include="Consul" Version="0.7.2.1" />
    <PackageReference Include="EntityFramework" Version="6.1.3" />
    <PackageReference Include="FluentValidation" Version="6.2.1.0" />
    <PackageReference Include="Microsoft.AspNetCore" Version="2.0.2" />
    <PackageReference Include="Newtonsoft.Json" Version="10.0.3" />
    <PackageReference Include="RabbitMQ.Client" Version="4.1.1" />
    <PackageReference Include="Serilog" Version="2.5.0" />
    <PackageReference Include="StackExchange.Redis" Version="1.2.0" />
    <PackageReference Include="System.Collections.Immutable" Version="1.4.0" />
    <PackageReference Include="System.Text.Encodings.Web" Version="4.4.0" />
    <PackageReference Include="System.Runtime.CompilerServices.Unsafe" Version="4.4.0" />
    <PackageReference Include="Microsoft.AspNet.WebApi.Client" Version="5.2.3" />
    <PackageReference Include="Microsoft.AspNet.WebApi.Core" Version="5.2.3" />
    <PackageReference Include="Microsoft.AspNet.WebApi.WebHost" Version="5.2.3" />
    <PackageReference Include="System.Security.Principal.Windows" Version="4.4.0" />
    <PackageReference Include="System.Threading.Tasks.Extensions" Version="4.4.0" />
  </ItemGroup>

  <!-- Assembly References -->
  <ItemGroup>
    <Reference Include="System.ServiceModel" />
    <Reference Include="System.Transactions" />
    <Reference Include="System.Web.DynamicData" />
    <Reference Include="System.Web.Entity" />
    <Reference Include="System.Web.ApplicationServices" />
    <Reference Include="System.ComponentModel.DataAnnotations" />
    <Reference Include="System" />
    <Reference Include="System.Data" />
    <Reference Include="System.Drawing" />
    <Reference Include="System.Web" />
    <Reference Include="System.Web.Extensions" />
    <Reference Include="System.Xml" />
    <Reference Include="System.Configuration" />
    <Reference Include="System.Web.Services" />
    <Reference Include="System.EnterpriseServices" />
    <Reference Include="System.Xml.Linq" />
    <Reference Include="System.Security.Principal.Windows">
      <HintPath>..\..\packages\System.Security.Principal.Windows.4.4.0\lib\net461\System.Security.Principal.Windows.dll</HintPath>
    </Reference>
    <Reference Include="System.Text.Encodings.Web">
      <HintPath>..\..\packages\System.Text.Encodings.Web.4.4.0\lib\netstandard2.0\System.Text.Encodings.Web.dll</HintPath>
    </Reference>
    <Reference Include="System.Threading.Tasks.Extensions">
      <HintPath>..\..\packages\System.Threading.Tasks.Extensions.4.4.0\lib\netstandard2.0\System.Threading.Tasks.Extensions.dll</HintPath>
    </Reference>
    <Reference Include="Toolkit, Version=17.2.3.0">
      <HintPath>..\..\packages\Toolkit.17.2.3\lib\net461\Toolkit.dll</HintPath>
      <Private>True</Private>
    </Reference>
    <Reference Include="System.Net.Http.Formatting">
      <HintPath>..\..\packages\Microsoft.AspNet.WebApi.Client.5.2.3\lib\net45\System.Net.Http.Formatting.dll</HintPath>
    </Reference>
    <Reference Include="System.Web.Http">
      <HintPath>..\..\packages\Microsoft.AspNet.WebApi.Core.5.2.3\lib\net45\System.Web.Http.dll</HintPath>
    </Reference>
    <Reference Include="System.Web.Http.WebHost">
      <HintPath>..\..\packages\Microsoft.AspNet.WebApi.WebHost.5.2.3\lib\net45\System.Web.Http.WebHost.dll</HintPath>
    </Reference>
  </ItemGroup>

  <!-- Content and None Includes -->
  <ItemGroup>
    <Content Include="Web.config">
      <CopyToOutputDirectory>Always</CopyToOutputDirectory>
    </Content>
    <Content Include="Data\RepositoryBase_Readme.txt" />
    <None Include="Readme Field Validation.txt" />
    <None Include="Readme Fluent Validation.txt" />
  </ItemGroup>

  <!-- Compile Files: Use wildcard to include all .cs files -->
  <ItemGroup>
    <Compile Include="**\*.cs" />
  </ItemGroup>

</Project>
