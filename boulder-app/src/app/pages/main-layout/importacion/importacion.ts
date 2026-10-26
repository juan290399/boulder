import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { ImportacionService } from '../../../services/importacion.service';
import { Plantilla, ResultadoImportacion } from '../../../models/importacion';

/**
 * Carga de datos desde las plantillas de Excel.
 *
 * La pantalla impone un orden y no lo deja saltear: primero se prueba, después
 * se carga. El botón de cargar no se habilita hasta que una simulación del
 * mismo archivo terminó, y cualquier cambio —otra plantilla, otro archivo— lo
 * vuelve a bloquear. No es una molestia gratuita: una plantilla de dos mil
 * renglones con cuarenta errores conviene descubrirla antes de escribir, y
 * quien la llenó necesita la lista de filas para corregirla.
 *
 * El informe distingue tres suertes y las tres importan. Aceptadas es lo que
 * entra. Rechazadas es lo que hay que arreglar, con el número de fila de Excel
 * para ir directo. Omitidas es lo que ya estaba: no es un error, es la prueba
 * de que volver a subir el mismo archivo no duplica nada.
 */
@Component({
  selector: 'app-importacion',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './importacion.html',
  styleUrl: './importacion.scss',
})
export class Importacion implements OnInit {

  private readonly servicio = inject(ImportacionService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly plantillas = signal<Plantilla[]>([]);
  readonly plantillaElegida = signal<Plantilla | null>(null);
  readonly archivo = signal<File | null>(null);

  readonly trabajando = signal(false);
  readonly error = signal<string | null>(null);
  readonly resultado = signal<ResultadoImportacion | null>(null);

  /**
   * La simulación que habilita la carga. Se guarda aparte del resultado
   * visible para que, tras aplicar, el botón no quede habilitado de nuevo.
   */
  readonly simulado = signal<string | null>(null);

  readonly puedeProbar = computed(() => !!this.plantillaElegida() && !!this.archivo() && !this.trabajando());

  readonly puedeAplicar = computed(() => {
    const r = this.resultado();
    return this.puedeProbar()
        && this.simulado() === this.firma()
        && !!r && !r.aplicado && r.totalAceptadas > 0;
  });

  ngOnInit(): void {
    this.servicio.plantillas().subscribe({
      next: p => {
        this.plantillas.set(p);
        this.cdr.detectChanges();
      },
      error: e => this.fallar(e),
    });
  }

  elegirPlantilla(clave: string): void {
    this.plantillaElegida.set(this.plantillas().find(p => p.clave === clave) ?? null);
    this.olvidar();
  }

  elegirArchivo(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    this.archivo.set(input.files?.length ? input.files[0] : null);
    this.olvidar();
  }

  probar(): void {
    this.correr(false);
  }

  aplicar(): void {
    this.correr(true);
  }

  private correr(aplicar: boolean): void {
    const plantilla = this.plantillaElegida();
    const archivo = this.archivo();
    if (!plantilla || !archivo) {
      return;
    }
    this.trabajando.set(true);
    this.error.set(null);
    this.cdr.detectChanges();

    this.servicio.importar(plantilla.clave, archivo, aplicar).subscribe({
      next: r => {
        this.resultado.set(r);
        this.simulado.set(aplicar ? null : this.firma());
        this.trabajando.set(false);
        this.cdr.detectChanges();
      },
      error: e => this.fallar(e),
    });
  }

  /** Identifica la combinación plantilla + archivo que se simuló. */
  private firma(): string {
    const p = this.plantillaElegida();
    const a = this.archivo();
    return `${p?.clave}|${a?.name}|${a?.size}|${a?.lastModified}`;
  }

  private olvidar(): void {
    this.resultado.set(null);
    this.simulado.set(null);
    this.error.set(null);
  }

  private fallar(e: unknown): void {
    const http = e as HttpErrorResponse;
    this.error.set(http?.error?.mensaje ?? http?.message ?? 'No se pudo completar la operación.');
    this.trabajando.set(false);
    this.cdr.detectChanges();
  }

  /** Porcentaje de una parte sobre el total leído, para la barra del resumen. */
  pct(parte: number, total: number): number {
    return total > 0 ? Math.round((parte / total) * 100) : 0;
  }
}
