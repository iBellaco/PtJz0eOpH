import json, re

with open("all_missing_keys.json", "r", encoding="utf-8") as f:
    keys = json.load(f)

print(f"Loaded {len(keys)} keys to translate.")

exact_map = {
    "Abrir": "Abrir",
    "Builds Creadas:": "Builds Criadas:",
    "Confirmar": "Confirmar",
    "DM": "DM",
    "Presupuesto": "Orçamento",
    "Reiniciar": "Reiniciar",
    "Volver": "Voltar",
    "Cerrar": "Fechar",
    "Aceptar": "Aceitar",
    "Cancelar": "Cancelar",
    "Eliminar": "Excluir",
    "Guardar": "Salvar",
    "Buscar": "Buscar",
    "Editar": "Editar",
    "Copiar": "Copiar",
    "Ver": "Ver",
    "Detalles": "Detalhes",
    "Información": "Informações",
    "Ajustes": "Configurações",
    "Configuración": "Configuração",
    "Historial": "Histórico",
    "Historial de Drafts": "Histórico de Drafts",
    "Tier List": "Tier List",
    "Campeones": "Campeões",
    "Objetos": "Itens",
    "Runas": "Runas",
    "Hechizos": "Feitiços",
    "Soporte": "Suporte",
    "Moderación": "Moderação",
    "Avisos": "Avisos",
    "Broadcast": "Broadcast",
    "Base Datos": "Banco de Dados",
    "CPM": "CPM",
    "Sponsor": "Patrocinador",
    "Patrocinador": "Patrocinador",
    "Mío": "Meu",
    "Champs": "Campeões",
    "Hist": "Histórico",
    "TOP": "TOPO",
    "JUNGLA": "SELVA",
    "MID": "MEIO",
    "ADC": "ATIRADOR",
    "SUPPORT": "SUPORTE",
    "PRO": "PRO",
    "Main": "Main",
    "Flex": "Flex",
    "LIVE": "AO VIVO",
    "ÚNICA POR LÍNEA": "ÚNICA POR ROTA",
    "Sinergia S+ / Tier Soberano": "Sinergia S+ / Tier Soberano",
    "Veredicto del Coach Soberano:": "Veredito do Coach Soberano:",
    "Asistente Táctico Oficial de Drafting": "Assistente Tático Oficial de Drafting",
    "Parche": "Patch",
    "1. Compatibilidad y Parche Oficial": "1. Compatibilidade e Patch Oficial",
    "Preguntas Frecuentes": "Perguntas Frequentes",
    "Buscar pregunta o duda...": "Buscar pergunta ou dúvida...",
    "No se encontraron preguntas que coincidan con tu búsqueda.": "Nenhuma pergunta encontrada para sua busca.",
    "Entendido": "Entendido",
    "Selección de Campeones": "Seleção de Campeões",
    "Tier List & Campeones": "Tier List & Campeões",
    "Catálogo": "Catálogo",
    "partidas guardadas": "partidas salvas",
    "Expandir barra de navegación": "Expandir barra de navegação",
    "Minimizar barra de navegación": "Minimizar barra de navegação",
    "Copia de Seguridad / Restaurar": "Copia de Segurança / Restauração",
    "Limpiar Historial": "Limpar Histórico",
    "Rendimiento de Segundo Plano": "Desempenho em Segundo Plano",
    "Configuración necesaria para que el asistente no se cierre": "Configuração necessária para o assistente não fechar",
    "Ventana Flotante (Overlay)": "Janela Flutuante (Sobreposição)",
    "Requerido para mostrar recomendaciones sobre el juego": "Necessário para exibir recomendações sobre o jogo",
    "Activar": "Ativar",
    "Ahorro de Batería": "Economia de Bateria",
    "Pon en 'Sin Restricciones' para no cerrarse": "Defina como 'Sem Restrições' para não fechar",
    "Recomendado: Activar Notificaciones": "Recomendado: Ativar Notificações",
    "Build y Tácticas": "Build e Táticas",
    "Quitar de Favoritos": "Remover dos Favoritos",
    "Marcar como Favorito": "Marcar como Favorito",
    "Cambiar Línea / Rol Activo:": "Mudar Rota / Função Ativa:",
    "Coach de Élite & Draft": "Coach de Elite & Draft",
    "Análisis Táctico en Tiempo Real": "Análise Tática em Tempo Real",
    "Selecciona composiciones óptimas con evaluación para los 5 roles de Wild Rift, sinergias de equipo, detección de counters y condición de victoria.": "Selecione composições ideais com avaliação para as 5 funções de Wild Rift, sinergias, detecção de counters e condição de vitória.",
    "Recomendador de 3 mejores picks": "Recomendador dos 3 melhores picks",
    "Wombo combos y balance de daño": "Wombo combos e balanço de dano",
    "Condición de victoria del equipo": "Condição de vitória da equipe",
    "Asistente Flotante en Juego": "Assistente Flutuante no Jogo",
    "Burbuja Flotante & Visión OCR": "Bolha Flutuante & Visão OCR",
    "Activa la burbuja flotante para recibir coaching en directo sobre la pantalla de Wild Rift y escanear el draft automáticamente sin salir del juego.": "Ative a bolha flutuante para receber coaching ao vivo sobre a tela de Wild Rift e escanear o draft automaticamente sem sair do jogo.",
    "Burbuja flotante movible": "Bolha flutuante móvel",
    "Apoyar el Proyecto": "Apoiar o Projeto",
    "Donaciones y Comunidad": "Doações e Comunidade",
    "Coach es una app sin anuncios molestos. Si la app te ayuda a subir de elo y ganar partidas, tu donación permite mantener los servidores y actualizaciones constantes.": "O Coach é um aplicativo sem anúncios invasivos. Se o app ajuda você a subir de elo e vencer partidas, sua doação ajuda a manter os servidores e atualizações constantes.",
    "Métodos de Donación Disponibles:": "Métodos de Doação Disponíveis:",
    "¡Gracias por apoyar a la comunidad!": "Obrigado por apoiar a comunidade!",
    "Cada aporte cuenta para seguir mejorando el asistente táctico, las builds y la precisión de análisis en tiempo real.": "Cada contribuição conta para continuar melhorando o assistente tático, as builds e a precisão das análises em tempo real.",
    "Entendido / Cerrar": "Entendido / Fechar",
    "INSTANTÁNEO": "INSTANTÂNEO",
    "Requisitos de contraseña:": "Requisitos de senha:",
    "- Mínimo 8 caracteres": "- Mínimo de 8 caracteres",
    "- Una letra mayúscula y minúscula": "- Uma letra maiúscula e minúscula",
    "- Un número y carácter especial": "- Um número e caractere especial",
    "Para poder iniciar sesión, deberás verificar tu correo electrónico con el enlace de confirmación que te enviaremos.": "Para fazer login, você precisará verificar seu e-mail pelo link de confirmação enviado.",
    "¿Ya tienes una cuenta? ": "Já tem uma conta? ",
    "Inicia sesión": "Faça login",
    "¿Olvidaste tu contraseña?": "Esqueceu sua senha?",
    "¿No tienes una cuenta? ": "Não tem una conta? ",
    "Regístrate": "Cadastre-se",
    "Logo": "Logo",
    "Mostrar contraseña": "Exibir senha",
    "Seguridad de contraseña:": "Segurança da senha:",
    "Se ha enviado un enlace de recuperación a tu correo electrónico.": "Um link de recuperação foi enviado para o seu e-mail.",
    "Volver a iniciar sesión": "Voltar ao login",
    "Bandeja de Entrada": "Caixa de Entrada",
    "Esencia Azul": "Essência Azul",
    "EA": "EA",
    "Esencia Naranja": "Essência Laranja",
    "EN": "EL",
    "Verificado": "Verificado",
    "Cuenta Verificada": "Conta Verificada",
    "Esta cuenta de invocador se encuentra verificada y autenticada oficialmente en el sistema.": "Esta conta de invocador está verificada e autenticada oficialmente no sistema.",
    "Estado: Perfil auténtico, protegido y sincronizado.": "Status: Perfil autêntico, protegido e sincronizado.",
    "Línea": "Rota",
    "Todas las Líneas": "Todas as Rotas",
    "Filtros / Buscar": "Filtros / Buscar",
    "Expandir filtros": "Expandir filtros",
    "Filtrar por Línea": "Filtrar por Rota",
    "Desliza": "Deslize",
    "Minimizar filtros": "Minimizar filtros",
    "Todas las Colas": "Todas as Filas",
    "Clasificatoria Normal": "Ranqueada Normal",
    "Clasificatoria Legendaria": "Ranqueada Lendária",
    "Atrás": "Voltar",
    "Siguiente": "Avançar",
    "¡Empezar!": "Começar!",
    "hace 24 horas": "há 24 horas",
    "hace 24h": "há 24h",
    "hace 12 horas": "há 12 horas",
    "hace 12h": "há 12h",
    "actual": "atual",
    "Evolución del Win Rate": "Evolução do Win Rate",
    "Tendencia en vivo": "Tendência ao vivo",
    "Imagen adjuntada correctamente": "Imagem anexada com sucesso",
    "Error al procesar la imagen": "Erro ao processar imagem",
    "La imagen excede el límite de 2 MB": "A imagem excede o limite de 2 MB",
    "Buzón de Reportes & Ideas": "Caixa de Relatórios & Ideias",
    "1. Selecciona Campeón:": "1. Selecione o Campeão:",
    "Seleccionado": "Selecionado",
    "Obligatorio": "Obrigatório",
    "Cambiar": "Mudar",
    "Toca para elegir del catálogo... (* Obligatorio)": "Toque para escolher do catálogo... (* Obrigatório)",
    "Efecto de partículas en la barra de navegación": "Efeito de partículas na barra de navegação",
    "Consejo del Coach Soberano": "Conselho do Coach Soberano",
    "¡Cada región altera la energía y colores de la interfaz! Selecciona tu región favorita para sincronizar tu estilo.": "Cada região altera a energia e as cores da interface! Selecione sua região favorita para sincronizar seu estilo.",
    "Explorador Visual de Regiones": "Explorador Visual de Regiões",
    "Panel de Moderación": "Painel de Moderação",
    "Panel de Administración": "Painel de Administração",
    "Control de Usuarios, Membresías y Slots": "Controle de Usuários, Membros e Slots"
}

def translate_es_to_pt(text):
    if text in exact_map:
        return exact_map[text]
        
    t = text
    t = t.replace("Línea:", "Rota:").replace("Línea", "Rota").replace("línea", "rota")
    t = t.replace("Campeón:", "Campeão:").replace("Campeón", "Campeão").replace("campeón", "campeão")
    t = t.replace("Campeones", "Campeões").replace("campeones", "campeões")
    t = t.replace("Habilidad", "Habilidade").replace("habilidad", "habilidade")
    t = t.replace("Habilidades", "Habilidades").replace("habilidades", "habilidades")
    t = t.replace("Definitiva", "Ultimate").replace("definitiva", "ultimate")
    t = t.replace("Selección", "Seleção").replace("selección", "seleção")
    t = t.replace("Configuración", "Configuração").replace("configuración", "configuração")
    t = t.replace("Suscripción", "Assinatura").replace("suscripción", "assinatura")
    t = t.replace("Notificación", "Notificação").replace("notificación", "notificação")
    t = t.replace("Notificaciones", "Notificações").replace("notificaciones", "notificações")
    t = t.replace("Descripción", "Descrição").replace("descripción", "descrição")
    t = t.replace("Información", "Informação").replace("información", "informação")
    t = t.replace("Atención", "Atenção").replace("atención", "atenção")
    t = t.replace("Publicación", "Publicação").replace("publicação", "publicação")
    t = t.replace("Sincronización", "Sincronização").replace("sincronización", "sincronização")
    t = t.replace("Protección", "Proteção").replace("protección", "proteção")
    t = t.replace("Restauración", "Restauração").replace("restauración", "restauração")
    t = t.replace("Verificación", "Verificação").replace("verificación", "verificação")
    t = t.replace("Calibración", "Calibração").replace("calibración", "calibração")
    t = t.replace("Detección", "Detecção").replace("detección", "detecção")
    t = t.replace("Asignación", "Atribuição").replace("asignación", "atribuição")
    t = t.replace("Redirección", "Redirecionamento").replace("redirección", "redirecionamento")
    t = t.replace("Evaluación", "Avaliação").replace("evaluación", "avaliação")
    t = t.replace("Duración", "Duração").replace("duración", "duração")
    t = t.replace("Sugerencia", "Sugestão").replace("sugerencia", "sugestão")
    t = t.replace("Recomendación", "Recomendação").replace("recomendación", "recomendação")
    t = t.replace("Posición", "Posição").replace("posición", "posição")
    t = t.replace("Condición", "Condição").replace("condición", "condição")
    t = t.replace("Excepción", "Exceção").replace("excepción", "exceção")
    t = t.replace("Transacción", "Transação").replace("transacción", "transação")
    t = t.replace("Rendimiento", "Desempenho").replace("rendimiento", "desempenho")
    t = t.replace("Invocador", "Invocador").replace("invocador", "invocador")
    t = t.replace("Invocadores", "Invocadores").replace("invocadores", "invocadores")
    t = t.replace("Pelea de equipo", "Luta em equipe").replace("pelea de equipo", "luta em equipe")
    t = t.replace("Peleas de equipo", "Lutas em equipe").replace("peleas de equipo", "lutas em equipe")
    t = t.replace("Daño Físico", "Dano Físico").replace("daño físico", "dano físico")
    t = t.replace("Daño Mágico", "Dano Mágico").replace("daño mágico", "dano mágico")
    t = t.replace("Daño Verdadero", "Dano Verdadeiro").replace("daño verdadero", "dano verdadeiro")
    t = t.replace("Daño", "Dano").replace("daño", "dano")
    t = t.replace("Aceleración", "Aceleração").replace("aceleración", "aceleração")
    t = t.replace("Enfriamiento", "Tempo de recarga").replace("enfriamiento", "tempo de recarga")
    t = t.replace("Curación", "Cura").replace("curación", "cura")
    t = t.replace("Sostenimiento", "Sustentação").replace("sostenimiento", "sustentação")
    t = t.replace("Resistencia Mágica", "Resistência Mágica").replace("resistencia mágica", "resistência mágica")
    t = t.replace("Penetración", "Penetração").replace("penetración", "penetração")
    t = t.replace("Velocidad de Ataque", "Velocidade de Ataque").replace("velocidad de ataque", "velocidade de ataque")
    t = t.replace("Velocidad de Movimiento", "Velocidade de Movimento").replace("velocidad de movimiento", "velocidade de movimiento")
    t = t.replace("Probabilidad de Crítico", "Chance de Crítico").replace("probabilidad de crítico", "chance de crítico")
    t = t.replace("Robo de Vida", "Roubo de Vida").replace("robo de vida", "roubo de vida")
    t = t.replace("Objeto", "Item").replace("objeto", "item").replace("Objetos", "Itens").replace("objetos", "itens")
    t = t.replace("Hechizo", "Feitiço").replace("hechizo", "feitiço").replace("Hechizos", "Feitiços").replace("hechizos", "feitiços")
    t = t.replace("Usuario", "Usuário").replace("usuario", "usuário").replace("Usuarios", "Usuários").replace("usuarios", "usuários")
    t = t.replace("Contraseña", "Senha").replace("contraseña", "senha")
    t = t.replace("Correo electrónico", "E-mail").replace("correo electrónico", "e-mail").replace("Correo", "E-mail").replace("correo", "e-mail")
    t = t.replace("Aceptar", "Aceitar").replace("aceptar", "aceitar")
    t = t.replace("Cancelar", "Cancelar").replace("cancelar", "cancelar")
    t = t.replace("Guardar", "Salvar").replace("guardar", "salvar")
    t = t.replace("Eliminar", "Excluir").replace("eliminar", "excluir")
    t = t.replace("Cerrar", "Fechar").replace("cerrar", "fechar")
    t = t.replace("Siguiente", "Avançar").replace("siguiente", "avançar")
    t = t.replace("Anterior", "Anterior").replace("anterior", "anterior")
    t = t.replace("Buscar", "Buscar").replace("buscar", "buscar")
    t = t.replace("Copiar", "Copiar").replace("copiar", "copiar")
    t = t.replace("Compartir", "Compartilhar").replace("compartir", "compartilhar")
    t = t.replace("Estadísticas", "Estatísticas").replace("estadísticas", "estatísticas")
    t = t.replace("Dispositivo", "Dispositivo").replace("dispositivo", "dispositivo")
    t = t.replace("Mensaje", "Mensagem").replace("mensaje", "mensagem").replace("Mensajes", "Mensagens").replace("mensajes", "mensagens")
    t = t.replace("Reporte", "Relatório").replace("reporte", "relatório").replace("Reportes", "Relatórios").replace("reportes", "relatórios")
    t = t.replace("y", "e") if " y " in t else t

    return t

translated_map = {}
for k in keys:
    translated_map[k] = translate_es_to_pt(k)

with open("pt_translations_bulk.json", "w", encoding="utf-8") as f:
    json.dump(translated_map, f, ensure_ascii=False, indent=2)

print(f"Successfully generated {len(translated_map)} PT translations in pt_translations_bulk.json!")
