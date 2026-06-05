package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CollaboratorRepository(private val collaboratorDao: CollaboratorDao) {
    val allCollaborators: Flow<List<Collaborator>> = collaboratorDao.getAllCollaborators()

    suspend fun insert(collaborator: Collaborator) {
        collaboratorDao.insertCollaborator(collaborator)
    }

    suspend fun update(collaborator: Collaborator) {
        collaboratorDao.updateCollaborator(collaborator)
    }

    suspend fun delete(collaborator: Collaborator) {
        collaboratorDao.deleteCollaborator(collaborator)
    }

    suspend fun prepopulateIfEmpty() {
        val currentList = allCollaborators.first()
        if (currentList.isEmpty()) {
            val defaults = listOf(
                Collaborator(
                    name = "Adriano Silva",
                    department = "Departamento de Logística",
                    company = "COF PT",
                    email = "adriano.silva@coficall.com",
                    phone = "+351 912 345 678",
                    photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA3wOgM10UqTzWPOs604F9plgM-ex-Sgwv0jjxif1sYzw_WoigeUO7MvPyqkZZzw_mtGgjAVpl5RcwduijPXZILH_NfWOVmtEHQj6ClILdjkRfwttgPrhmRAZ6tFQ5or5Xz0PncTDMlBMtF2ACol8F5kGY5te9BE_24tAXEQoA62x4WHA-xvaeqExT6yJFRUZq8LU8DartHUJVmueA72StXnSbplqCUqQYo_9NiFwE6ZA2G3k4QdbIU0e4SGU_rL1IStNFyGf-X6Zs",
                    isFavorite = false,
                    status = "online",
                    isFactory = true
                ),
                Collaborator(
                    name = "Ana Martins",
                    department = "Recursos Humanos",
                    company = "COF PT",
                    email = "ana.martins@coficall.com",
                    phone = "+351 912 345 679",
                    photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBDABl5AQjX7AzrWpfarBUC7oQnMviVN1Wds4B8yiIAC9ybQoOx4wTiw5wwx80uPcKX1b0ttE9dM0rbH2QixGLIxcelMc-xkFCNIQG4GruIv39LBo5Lw-Fv0b3KQHU0tUa1qIVUXTbfGkum3UHtT_M524CjKHkRcSc-1lnIf5Yy15_FfaFqz6L9ZwAb0ic7J1JcxwU8XZNm2lb1L7yHJhchWcWiGHPsHDMDefpJADlonLq1V-1V7qiYyRAS62L9JRxo9TT35TcfmeM",
                    isFavorite = true,
                    status = "offline",
                    isFactory = true
                ),
                Collaborator(
                    name = "Bernardo Mendes",
                    department = "Engenharia de Redes",
                    company = "COF GR",
                    email = "bernardo.mendes@coficall.com",
                    phone = "+30 210 123456",
                    photoUrl = "", // Empty to test initials-based placeholder
                    isFavorite = false,
                    status = "online",
                    isFactory = true
                ),
                Collaborator(
                    name = "Carlos Ferreira",
                    department = "Operações de Campo",
                    company = "CoE PT",
                    email = "carlos.ferreira@coficall.com",
                    phone = "+351 912 345 680",
                    photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDYiMjN48Q3Eqryl7gkxXBtAY73_BeJu30rAlbmOMjrdzfjz8EtoQ-6s8zkeR3TKPkKL47B9QlJN9ftqhZ5mBSQogsWA5q-tYaj7NAmu17Y6x5SINuHx7e3XiQCJVatHqUmrzIFTc3o8ihKdUAx9lxOK2i32nzJSJ__ki88alUMtMNhMVrLFIgSGRMOHJMzyoqY6n-EybZW5ps-jM-QYLCRr763Bs12qCDRILUN9MozxpIyjU8sUs5_2ETtC8UX0-y5df5JJQRGYbg",
                    isFavorite = false,
                    status = "offline",
                    isOffice = true
                ),
                Collaborator(
                    name = "Catarina Santos",
                    department = "Marketing Digital",
                    company = "CoE GR",
                    email = "catarina.santos@coficall.com",
                    phone = "+30 210 123457",
                    photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCfWzWf3BbeSmNcQ9WUzqWYR8pdMwrRiE2yGtGQFB33xRkGDtsfYmRHNuP04TKmeEa-p6xhaGNmiq-E84UiuHNUjB68G5k7WfEtJhEu6zleuGAoAJNbFNZ_6GTpuCXY5B8wQxdmB8ov5xJHQtesrSZIs19MTuw5wLVnGZbbvBNwcVfZwv49zzvzm1FViFN6tfY4rrGLf49oVQolaHHWi8pdYmkgtv0FNOIbLBtK1LS_GAXnnjzWDBMKAkdJ24rG15vFsUpiBM1cJAk",
                    isFavorite = false,
                    status = "online",
                    isOffice = true
                ),
                Collaborator(
                    name = "Carlos Mendes",
                    department = "Gestor de Projetos",
                    company = "CofiCall Global",
                    email = "carlos.mendes@coficall.com",
                    phone = "+351 912 345 681",
                    photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBmDJWXe_T_JiGLOuacY3wgkQf6v2bOko7SHXvK-ZGJocZqB5bmFtSDHOhWmRRtZPm1hfbNTA70ZHLyEUsMjWEzXVT3WAb1xe9860ZMMWVtH5gZVeGv3RM6M_rVzg_zcHXBcIHNnL-ZAfx4epc82-IYpAtDV1cD6OWMKN1wDQvLG45R0nFwxHKI0A6GWVYZguUkZMvAkwLGUCLqMR68PMfq-zzDtEkHucHKSlB_Hfo8SO9QO03SwxgBFF4MXqmHFWQjujlmEQjgHwI",
                    isFavorite = true,
                    status = "online",
                    isOffice = true
                ),
                Collaborator(
                    name = "Ana Silveira",
                    department = "UX Designer Sénior",
                    company = "CofiCall Tech",
                    email = "ana.silveira@coficall.com",
                    phone = "+351 912 345 682",
                    photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCWNqtkDpm10vQF0C5aUNwwDwscv4NmSMaxkHfdBfD3B5lyiOtcB9u7qrDuR76qnktEkCynPt5wc1HvVbi-PwBYkmM9_N6diV_PFyZii2nSTGi43zvmhAzmnu8n2Xxnd9LeNK72uIwucUKiBVxiH8rizmQIdpRniuMmqywQV0ZUCZo4Sg2nhw5hHqh7q4s6T8Jzuc1xxxp__CpvkDXvFzfN6gVCF9RtENvHiwSCY1Lz-N-JqnNL4tfkGIIlv2PzytgfprxxBaVd3E4",
                    isFavorite = true,
                    status = "online",
                    isOffice = true
                ),
                Collaborator(
                    name = "Ricardo Oliveira",
                    department = "Engenheiro DevOps",
                    company = "Infrastructure Hub",
                    email = "ricardo.oliveira@coficall.com",
                    phone = "+351 912 345 683",
                    photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuABxhdXgDNVhdWTUOenYQWc8XvY01P9z1lSD-FN2yKauPbA00CYXVMaS6ck6OhD4SXqFgwqpOJRRx9RcS21BP13MxT37b6Opu1H1GZBhgHtASwdjHkKXyShtAy58TcVOWLSnaizWGdBH1XoXrfYdj4UznX1Rpsq93r07hXdHK7wZFnmE4BLUyuqA4Bg3VYJq81jGsjp9Lo6rcgWJmDeZHsFyI7dJAlW8bYsE42Mr1UuTfhb2xU1r8X5lvv4X9j1AnA7qjamOIRz2g8",
                    isFavorite = true,
                    status = "offline",
                    isOffice = true
                ),
                Collaborator(
                    name = "Sofia Rocha",
                    department = "Coordenadora de RH",
                    company = "CofiCall People",
                    email = "sofia.rocha@coficall.com",
                    phone = "+351 912 345 684",
                    photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD0DYuiDUX15ljuJr7KZ2TIlqF2RGu-NDhhWJIOiIAMXsd5Xu6XWvcAJL2fP6vwTQNXzucvjzhkZOEATmPWtSYf5rxvpz0ptGC0hB4Cw6NWOInPAs5AVtFiVtwxu5qwVp9If922bDm54Ip8Qn2zmaDJlBAIf2axCPl51LLfIwKJ38VJM9bD8x7MgLz2vPlz_ElMFuKpdO4wIr6lfWoTEcnrOSQjXkPrLFG7x4z0Y9_u4gBNXbBEEhIqhSBbPl5YQEXtGjVBRSN9GPI",
                    isFavorite = true,
                    status = "online",
                    isOffice = true
                )
            )
            collaboratorDao.insertCollaborators(defaults)
        }
    }
}
