package com.example.coficall.model

data class Collaborator(
    val id: String,
    val name: String,
    val jobTitle: String,
    val department: String,
    val businessUnit: String,
    val phone: String? = null,
    val extension: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isFavorite: Boolean = false,
    val isOnline: Boolean = false,
    val pendingName: String? = null,
    val pendingJobTitle: String? = null,
    val pendingDepartment: String? = null,
    val pendingPhone: String? = null,
    val pendingExtension: String? = null,
    val pendingEmail: String? = null,
    val hasPendingChanges: Boolean = false,
)


data class BusinessUnit(
    val id: String,
    val name: String,
    val shortName: String,
    val country: String,
    val collaboratorCount: Int,
    val type: BusinessUnitType,
)

enum class BusinessUnitType { UNIT, FACTORY, OFFICE }

// Sample data aligned exactly with the screenshots count and name layouts
val sampleCollaborators = listOf(
    Collaborator("1", "Adriano Silva", "Departamento de Logística", "Logística", "COF PT", "+351 912 345 678", "101", "adriano.silva@coficab.com", isFavorite = false, isOnline = true, photoUrl = "https://randomuser.me/api/portraits/men/1.jpg"),
    Collaborator("2", "Ana Martins", "Recursos Humanos", "RH", "COF PT", "+351 913 456 789", "102", "ana.martins@coficab.com", isFavorite = true, isOnline = false, photoUrl = "https://randomuser.me/api/portraits/women/2.jpg"),
    Collaborator("3", "Bernardo Mendes", "Engenharia de Redes", "IT", "COF GR", "+30 691 234 567", "201", "bernardo.mendes@coficab.com", isFavorite = false, isOnline = true, photoUrl = null),
    Collaborator("4", "Carlos Ferreira", "Operações de Campo", "Operações", "CoE PT", "+351 914 567 890", "103", "carlos.ferreira@coficab.com", isFavorite = false, isOnline = false, photoUrl = "https://randomuser.me/api/portraits/men/3.jpg"),
    
    // Favorites and Online status for other screens
    Collaborator("5", "Carlos Mendes", "Gestor de Projetos", "Projetos", "COFICALL GLOBAL", "+351 915 678 901", "104", "carlos.mendes@coficab.com", isFavorite = true, isOnline = true, photoUrl = "https://randomuser.me/api/portraits/men/4.jpg"),
    Collaborator("6", "Catarina Santos", "Marketing Digital", "Marketing", "COF PT", "+351 916 789 012", "105", "catarina.santos@coficab.com", isFavorite = false, isOnline = true, photoUrl = "https://randomuser.me/api/portraits/women/5.jpg"),
    Collaborator("7", "Ana Silveira", "UX Designer Sénior", "Design", "COFICALL TECH", "+351 917 890 123", "106", "ana.silveira@coficab.com", isFavorite = true, isOnline = true, photoUrl = "https://randomuser.me/api/portraits/women/6.jpg"),
    Collaborator("8", "Ricardo Oliveira", "Engenheiro DevOps", "IT", "INFRASTRUCTURE HUB", "+351 918 901 234", "107", "ricardo.oliveira@coficab.com", isFavorite = true, isOnline = false, photoUrl = "https://randomuser.me/api/portraits/men/7.jpg"),
    Collaborator("9", "Sofia Rocha", "Coordenadora de RH", "RH", "COFICALL PEOPLE", "+351 919 012 345", "108", "sofia.rocha@coficab.com", isFavorite = true, isOnline = true, photoUrl = "https://randomuser.me/api/portraits/women/9.jpg"),
    
    Collaborator("10", "Diana Costa", "Contabilidade", "Financeiro", "CoE GR", "+351 921 234 567", "110", "diana.costa@coficab.com", isFavorite = false, isOnline = false, photoUrl = "https://randomuser.me/api/portraits/women/10.jpg")
)

val sampleBusinessUnits = listOf(
    BusinessUnit("bu1", "Portugal", "COF PT", "Portugal", 2, BusinessUnitType.UNIT),
    BusinessUnit("bu2", "Portugal", "COF GR", "Portugal", 1, BusinessUnitType.UNIT),
    BusinessUnit("bu3", "Centro de Excelência PT", "CoE PT", "Portugal", 1, BusinessUnitType.FACTORY),
    BusinessUnit("bu4", "Centro de Excelência GR", "CoE GR", "Grécia", 1, BusinessUnitType.FACTORY),
    
    // Office placeholder to populate the Offices tab if clicked
    BusinessUnit("bu5", "Escritório Central Lisboa", "Esc. LIS", "Portugal", 0, BusinessUnitType.OFFICE)
)

data class DepartmentInfo(
    val name: String,
    val collaboratorCount: Int
)

data class AppUpdateInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val apkUrl: String,
    val forceUpdate: Boolean
)
