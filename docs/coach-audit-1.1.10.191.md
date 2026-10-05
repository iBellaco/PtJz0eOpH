# Auditoría Técnica y Registro de Modificaciones — Versión 1.1.10.191 (Build 907)

## Resumen Ejecutivo
Esta versión incorpora el cálculo dinámico y cobro de la comisión de red blockchain a cargo del usuario en Esencia Naranja (EN) al solicitar canjes a USDT, valida que el saldo cubra tanto el importe a canjear como la comisión requerida, y añade en el panel de administración un historial de 7 días de pagos USDT procesados.

## Modificaciones Principales

### 1. Comisión de Red en Canjes de Esencia Naranja (USDT)
- **Cálculo en Esencia Naranja (EN):** Se define la comisión de red según la red USDT seleccionada (BEP20: 1 EN, TRC20: 2 EN, ERC20: 5 EN).
- **Validación de saldo total:** El sistema verifica que el usuario disponga de saldo suficiente para cubrir el importe de canje más la comisión de red (`monto + comisión = total EN`).
- **Desglose transparente:** La interfaz de selección y confirmación de canje detalla el importe en USDT a recibir, la comisión de red a cargo del usuario en EN y el total de esencias a descontar.
- **Reembolso íntegro en rechazo:** En caso de que una solicitud sea rechazada por administración, se devuelven al usuario tanto el importe como la comisión descontada.

### 2. Panel de Administración — Historial de 7 Días de Pagos USDT
- **Pestañas de gestión:** Se dividió la vista de pagos USDT en `Pendientes` e `Historial (7 días)`.
- **Registro histórico:** El historial muestra todas las operaciones de canje resueltas en los últimos 7 días con fecha, estado (`PAGADO` o `RECHAZADO Y REEMBOLSADO`), usuario, red, comisión, billetera y acceso a comunicación privada directa.

### 3. Internacionalización y Depuración
- Se integraron las traducciones correspondientes en portugués para las etiquetas de comisión de red e historial de 7 días.
- Se incrementó el número de compilación interno (`versionCode 907`) y la versión pública (`versionName 1.1.10.191`).
