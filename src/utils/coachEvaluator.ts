import { Champion, DraftSlot, Language } from '../types';

export interface CoachAudit {
  diagnostic: string;
  sovereignDecision: string;
  microMacroDetail: string;
  applicableRule: string;
  synergyRating: number; // 0 to 100
  damageDistribution: { physical: number; magic: number; trueDamage: number };
  crowdControlScore: number; // 0 to 100
  frontlineScore: number; // 0 to 100
  keyAdvice: string[];
}

export function evaluateDraft(
  allySlots: DraftSlot[],
  enemySlots: DraftSlot[],
  lang: Language
): CoachAudit {
  const allyPicks = allySlots.filter((s) => s.champion !== null).map((s) => s.champion as Champion);
  const enemyPicks = enemySlots.filter((s) => s.champion !== null).map((s) => s.champion as Champion);

  // Damage distribution
  let physicalCount = 0;
  let magicCount = 0;
  let trueCount = 0;

  allyPicks.forEach((champ) => {
    if (champ.damageType === 'PHYSICAL') physicalCount += 1;
    else if (champ.damageType === 'MAGIC') magicCount += 1;
    else trueCount += 1;
  });

  const totalAlly = Math.max(1, allyPicks.length);
  const damageDistribution = {
    physical: Math.round((physicalCount / totalAlly) * 100),
    magic: Math.round((magicCount / totalAlly) * 100),
    trueDamage: Math.round((trueCount / totalAlly) * 100),
  };

  // Frontline check
  const frontlinePicks = allyPicks.filter(
    (c) => c.primaryRole === 'TOP' || c.primaryRole === 'SUPPORT' || c.isFrontline
  );
  const frontlineScore = Math.min(100, Math.round((frontlinePicks.length / 2) * 100));

  // Synergy check
  let synergyMatches = 0;
  allyPicks.forEach((champ) => {
    allyPicks.forEach((other) => {
      if (champ.id !== other.id && champ.synergies.includes(other.name)) {
        synergyMatches += 1;
      }
    });
  });
  const synergyRating = Math.min(100, 50 + synergyMatches * 15 + (frontlinePicks.length > 0 ? 15 : -10));

  // Counter check
  let counteredCount = 0;
  allyPicks.forEach((champ) => {
    enemyPicks.forEach((enemy) => {
      if (champ.counteredBy.includes(enemy.name)) {
        counteredCount += 1;
      }
    });
  });

  const crowdControlScore = Math.min(
    100,
    30 + allyPicks.filter((c) => c.primaryRole === 'SUPPORT' || c.primaryRole === 'JUNGLE' || c.primaryRole === 'TOP').length * 20
  );

  const isEs = lang === 'es';

  // Construct Sovereign Coach 4-step Audit
  let diagnostic = '';
  let sovereignDecision = '';
  let microMacroDetail = '';
  let applicableRule = '';
  const keyAdvice: string[] = [];

  if (allyPicks.length === 0) {
    diagnostic = isEs
      ? 'Fase de selección abierta sin asignaciones activas. La falta de ancla inicial concede la iniciativa de contra-elección al rival.'
      : 'Fase de seleção aberta sem atribuições ativas. A ausência de âncora inicial concede a iniciativa de contra-escolha ao rival.';
    sovereignDecision = isEs
      ? 'Asegurar una primera elección flexible de alta prioridad de línea o asegurar un soporte/jungla con alta presencia de mapa.'
      : 'Garantir uma primeira escolha flexível de alta prioridade de rota ou assegurar um suporte/caçador com presença ativa no mapa.';
    microMacroDetail = isEs
      ? 'Sincronizar el primer tempo de regreso a base al minuto 1:40 para comprar primer componente antes del Escarabajo de la Grieta (1:25 - 1:45).'
      : 'Sincronizar o primeiro tempo de retorno à base aos 1:40 para comprar primeiro componente antes do Aronguejo (1:25 - 1:45).';
    applicableRule = isEs
      ? 'Regla de Oro: En primera rotación, bloquea campeones con múltiples asignaciones de línea para no revelar la condición de victoria.'
      : 'Regra de Ouro: Na primeira rotação, bloqueie campeões com múltiplas funções para não revelar a condição de vitória.';
  } else if (damageDistribution.physical >= 80) {
    diagnostic = isEs
      ? 'Desequilibrio crítico de daño: 80%+ de perfil físico. La itemización defensiva del rival (Malla de espinas, Presagio de Randuin) anulará el pico de poder en el juego medio.'
      : 'Desequilíbrio crítico de dano: 80%+ de perfil físico. A itemização defensiva do rival anulará o pico de poder no meio de jogo.';
    sovereignDecision = isEs
      ? 'Incorporar daño mágico de escalado en Carril Central o Soporte de utilidad mágica para forzar dispersión de resistencias.'
      : 'Incorporar dano mágico de escalonamento no Meio ou Suporte com utilidade mágica para forçar dispersão de defesas.';
    microMacroDetail = isEs
      ? 'Obligatoria Cuchilla Negra en el primer iniciador al minuto 4:30 antes del primer Dragón Elemental (5:00).'
      : 'Cutelo Negro obrigatório no primeiro iniciador aos 4:30 antes do primeiro Dragão Elemental (5:00).';
    applicableRule = isEs
      ? 'Regla Soberana: Jamás cierres el draft sin al menos 30% de amenaza mágica o penetración mixta porcentual.'
      : 'Regra Soberana: Nunca feche o draft sem ao menos 30% de ameaça mágica ou penetração mista percentual.';
    keyAdvice.push(
      isEs
        ? 'Añadir fuente de daño mágico para evitar el apilamiento de armadura rival.'
        : 'Adicionar fonte de dano mágico para evitar acúmulo de armadura inimiga.'
    );
  } else if (frontlineScore < 40) {
    diagnostic = isEs
      ? 'Carencia de línea frontal y control de visión en cuellos de botella del río hacia Dragón y Heraldo.'
      : 'Carência de linha de frente e controle de visão em gargalos do rio rumo ao Dragão e Arauto.';
    sovereignDecision = isEs
      ? 'Cambiar el estilo de combate de choque directo (5v5 frontal) a emboscadas en niebla de guerra y empuje lateral (1-3-1 / 1-4).'
      : 'Mudar o estilo de luta de choque frontal para emboscadas na névoa de guerra e avanço lateral (1-3-1 / 1-4).';
    microMacroDetail = isEs
      ? 'Crash de oleada en carriles laterales 45 segundos antes de que aparezca el objetivo neutral; colocar lente de oráculo.'
      : 'Crash de tropas nas rotas laterais 45 segundos antes do objetivo neutro surgir; posicionar lente detectora.';
    applicableRule = isEs
      ? 'Regla de Visión: Si no puedes absorber daño frontal, gana la pelea por tempo de llegada y zonificación previa.'
      : 'Regra de Visão: Se você não pode absorver dano frontal, vença a luta pelo tempo de chegada e controle de zona prévio.';
    keyAdvice.push(
      isEs
        ? 'Evitar peleas 5v5 en río abierto; buscar escaramuzas con ventaja numérica.'
        : 'Evitar confrontos 5v5 em rio aberto; buscar emboscadas com vantagem numérica.'
    );
  } else {
    diagnostic = isEs
      ? `Composición equilibrada (${damageDistribution.physical}% Físico / ${damageDistribution.magic}% Mágico / ${damageDistribution.trueDamage}% Híbrido). Sinergia estimada en ${synergyRating}%.`
      : `Composição equilibrada (${damageDistribution.physical}% Físico / ${damageDistribution.magic}% Mágico / ${damageDistribution.trueDamage}% Híbrido). Sinergia estimada em ${synergyRating}%.`;
    sovereignDecision = isEs
      ? 'Jugar alrededor del carril con mayor prioridad para asegurar Heraldo de la Grieta al minuto 5:00 y abrir primera torre.'
      : 'Jogar ao redor da rota com maior prioridade para garantir Arauto do Vale aos 5:00 e abrir primeira torre.';
    microMacroDetail = isEs
      ? 'Slow push en oleada de minuto 4:15 para acumular dos oleadas y crashear bajo torre al 4:50, liberando la rotación hacia el río.'
      : 'Slow push na leva dos 4:15 para acumular duas levas e colidir sob a torre aos 4:50, liberando a rotação ao rio.';
    applicableRule = isEs
      ? 'Regla de Oleadas: El Heraldo no se disputa peleando el foso; se asegura congelando o crasheando las líneas antes del spawn.'
      : 'Regra de Levas: O Arauto não se disputa lutando o fosso; assegura-se controlando as rotas antes do surgimento.';
    keyAdvice.push(
      isEs
        ? 'Controlar la oleada previa a los 5:00 para asegurar prioridad en objetivos neutrales.'
        : 'Controlar a leva antes dos 5:00 para garantir prioridade em objetivos neutros.'
    );
  }

  if (counteredCount > 2) {
    keyAdvice.push(
      isEs
        ? 'Picks aliados con desfavor en emparejamientos directos: priorizar congelación de línea cerca de tu torre y pedir gankeo temprano.'
        : 'Escolhas aliadas em desvantagem de confronto: priorizar congelamento perto da torre e solicitar rotação inicial do caçador.'
    );
  }

  return {
    diagnostic,
    sovereignDecision,
    microMacroDetail,
    applicableRule,
    synergyRating,
    damageDistribution,
    crowdControlScore,
    frontlineScore,
    keyAdvice,
  };
}
