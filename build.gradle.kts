plugins {
	java
	jacoco
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.boulder"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

	// Lectura de las plantillas .xlsx que alimentan el importador. Solo se usa
	// poi-ooxml (formato moderno); el .xls binario no se acepta a propósito,
	// porque las plantillas llevan validación de datos y eso es xlsx.
	implementation("org.apache.poi:poi-ooxml:5.5.1")

	// Librerías de apoyo:
	// - Guava: caché con expiración para limitar intentos de inicio de sesión y
	//   precondiciones en los servicios.
	// - Commons Lang: normalización de texto (acentos, espacios, relleno) para
	//   reconocer un mismo sondaje escrito de formas distintas.
	// - Commons IO: nombres y extensiones de archivo seguros en el almacén de
	//   fotos, sin rutas que se salgan de la carpeta.
	implementation("com.google.guava:guava:33.4.8-jre")
	implementation("org.apache.commons:commons-lang3:3.18.0")
	implementation("commons-io:commons-io:2.21.0")

	implementation("org.postgresql:postgresql:42.6.0")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok") 

	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("io.jsonwebtoken:jjwt-api:0.13.0")
	implementation("com.fasterxml.jackson.core:jackson-databind")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
}

// Spring resuelve @PathVariable("id") y @RequestParam("area") por el nombre del
// parámetro cuando no se declara explícitamente, y ese nombre solo sobrevive en
// el bytecode si se compila con -parameters. El plugin de Spring Boot ya lo
// añade, pero se deja explícito porque es un requisito del código, no un detalle
// del plugin: sin la bandera, todos los endpoints con parámetros fallan en
// tiempo de ejecución. El equivalente para el compilador del IDE está en
// .settings/org.eclipse.jdt.core.prefs.
tasks.withType<JavaCompile> {
	options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
	useJUnitPlatform()
	// Cada corrida de pruebas deja el informe de cobertura actualizado.
	finalizedBy(tasks.named("jacocoTestReport"))
}

// Cobertura de las pruebas con JaCoCo: build/reports/jacoco/test/html/index.html.
tasks.named<JacocoReport>("jacocoTestReport") {
	dependsOn(tasks.named("test"))
	reports {
		html.required = true
		xml.required = true
	}
}

// Documentación técnica con Javadoc: build/docs/javadoc/index.html.
// Se omiten los avisos por comentarios faltantes en getters y métodos obvios;
// los errores de sintaxis de Javadoc siguen haciendo fallar la tarea.
tasks.named<Javadoc>("javadoc") {
	val opciones = options as StandardJavadocDocletOptions
	opciones.encoding = "UTF-8"
	opciones.charSet = "UTF-8"
	opciones.docEncoding = "UTF-8"
	opciones.locale = "es"
	opciones.windowTitle = "Boulder Drilling · API"
	opciones.docTitle = "Boulder Drilling — documentación técnica del backend"
	opciones.links("https://docs.oracle.com/en/java/javase/21/docs/api/")
	opciones.addStringOption("Xdoclint:all,-missing", "-quiet")
	opciones.tags("apiNote:a:Nota de la API:", "implNote:a:Nota de implementación:")
}
