package com.boulder.boulder.dominio;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Código canónico de un sondaje: {@code DDH-<PROYECTO>-<SECTOR>-<NNN>-<AA>},
 * por ejemplo {@code DDH-VERO-S-038-26}.
 *
 * <p>En los reportes de campo el mismo sondaje aparece como «Sondaje-vero-s-38-26»,
 * «VERO S 38_26» o «DDH-VERO-S039-26». Sin un identificador común no se puede
 * cruzar un reporte con otro, que es el problema que Boulder resuelve. Esta
 * clase reconoce esas variantes y las lleva a la forma canónica.</p>
 *
 * <p>Si al texto le falta el proyecto, el sector, el número o el año (por
 * ejemplo «DDH-35»), no se adivina: se devuelve vacío y quien llama decide.
 * Un sondaje mal asignado es peor que uno sin asignar.</p>
 */
public final class CodigoSondaje {

    /** Tras limpiar: [SONDAJE] [DDH] PROYECTO SECTOR NNN AA, separados por espacios. */
    private static final Pattern FORMA = Pattern.compile(
            "^(?:SONDAJE\\s+)?(?:DDH\\s+)?([A-Z]{2,})\\s+([A-Z])\\s*(\\d{1,3})\\s+(\\d{2})$");

    private CodigoSondaje() {
    }

    /**
     * Lleva un código escrito a mano a su forma canónica.
     *
     * @param escrito el código tal como aparece en el reporte; puede ser {@code null}
     * @return el código canónico, o vacío si el texto no identifica un sondaje sin ambigüedad
     */
    public static Optional<String> normalizar(String escrito) {
        if (StringUtils.isBlank(escrito)) {
            return Optional.empty();
        }
        // Acentos fuera, mayúsculas y cualquier separador (guion, guion bajo,
        // punto) convertido en un solo espacio.
        String limpio = StringUtils.normalizeSpace(
                StringUtils.upperCase(StringUtils.stripAccents(escrito)).replaceAll("[^A-Z0-9]+", " "));
        Matcher m = FORMA.matcher(limpio);
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(String.join("-", "DDH", m.group(1), m.group(2),
                StringUtils.leftPad(m.group(3), 3, '0'), m.group(4)));
    }

    /**
     * Indica si dos escrituras corresponden al mismo sondaje. Dos textos que no
     * se pueden normalizar nunca son el mismo sondaje, aunque sean idénticos.
     *
     * @param a primera escritura
     * @param b segunda escritura
     * @return {@code true} si ambas se normalizan al mismo código canónico
     */
    public static boolean mismoSondaje(String a, String b) {
        Optional<String> ca = normalizar(a);
        return ca.isPresent() && Objects.equals(ca, normalizar(b));
    }
}
