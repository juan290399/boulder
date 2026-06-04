# Boulder - Sistema de Gestión de Perforación Geológica

## 📌 Descripción

Boulder es una plataforma web orientada a la gestión de campañas de perforación geológica. Permite administrar proyectos, sondajes, máquinas, plataformas y el registro de información geológica generada durante el proceso de exploración.

El sistema centraliza la información operacional y geológica, mejorando la trazabilidad de los datos y facilitando la generación de reportes.

---

## 🚧 Estado del Proyecto

**Avance actual:** 40% de los Casos de Uso implementados.

El sistema se encuentra en desarrollo con los módulos base de administración, gestión operacional y estructura inicial del sistema.

---

## 🧩 Casos de Uso del Sistema

### 🔐 Seguridad y Administración

- UC1: Iniciar Sesión  
- UC2: Gestionar Usuarios  
- UC3: Gestionar Roles  
- UC4: Gestionar Diccionarios  

---

### 🏗️ Gestión Operacional

- UC5: Gestionar Proyectos  
- UC6: Gestionar Máquinas  
- UC7: Gestionar Plataformas  
- UC8: Gestionar Sondajes  

---

### ⛏️ Registro Geológico

- UC9: Registrar Litología  
- UC10: Registrar Alteración  
- UC11: Registrar Mineralización  
- UC12: Registrar Estructural  
- UC13: Registrar Geomecánica  
- UC14: Registrar Geoquímica  

---

### 📊 Control de Avances

- UC15: Registrar Avance Perforación  
- UC16: Registrar Avance Logueo  
- UC17: Registrar Avance Fotografía  
- UC18: Registrar Avance Corte  
- UC19: Registrar Avance Muestreo  
- UC20: Registrar Avance Densidad  
- UC21: Registrar Muestras  

---

### 📈 Visualización

- UC22: Visualizar Dashboard Principal  
- UC23: Visualizar Sondajes  
- UC24: Visualizar Máquinas  
- UC25: Visualizar Logueo  
- UC26: Visualizar Geoquímica  

---

### 📑 Reportes

- UC27: Generar Reportes  
- UC28: Exportar PDF / Excel  

---

## 📐 Diagrama de Casos de Uso (PlantUML)

```plantuml
@startuml

left to right direction

actor Administrador
actor "Geólogo Supervisor" as Supervisor
actor "Asistente de Geología" as Asistente
actor Cliente

rectangle Boulder {

    usecase "Iniciar Sesión" as UC1
    usecase "Gestionar Usuarios" as UC2
    usecase "Gestionar Roles" as UC3
    usecase "Gestionar Diccionarios" as UC4

    usecase "Gestionar Proyectos" as UC5
    usecase "Gestionar Máquinas" as UC6
    usecase "Gestionar Plataformas" as UC7
    usecase "Gestionar Sondajes" as UC8

    usecase "Registrar Litología" as UC9
    usecase "Registrar Alteración" as UC10
    usecase "Registrar Mineralización" as UC11
    usecase "Registrar Estructural" as UC12
    usecase "Registrar Geomecánica" as UC13
    usecase "Registrar Geoquímica" as UC14

    usecase "Registrar Avance Perforación" as UC15
    usecase "Registrar Avance Logueo" as UC16
    usecase "Registrar Avance Fotografía" as UC17
    usecase "Registrar Avance Corte" as UC18
    usecase "Registrar Avance Muestreo" as UC19
    usecase "Registrar Avance Densidad" as UC20
    usecase "Registrar Muestras" as UC21

    usecase "Visualizar Dashboard Principal" as UC22
    usecase "Visualizar Sondajes" as UC23
    usecase "Visualizar Máquinas" as UC24
    usecase "Visualizar Logueo" as UC25
    usecase "Visualizar Geoquímica" as UC26

    usecase "Generar Reportes" as UC27
    usecase "Exportar PDF/Excel" as UC28
}

@enduml