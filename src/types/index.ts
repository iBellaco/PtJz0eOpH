export type Role = 'TOP' | 'JUNGLE' | 'MID' | 'ADC' | 'SUPPORT';

export type DamageType = 'PHYSICAL' | 'MAGIC' | 'TRUE_HYBRID';

export interface ChampionSkill {
  slot: string; // P, 1, 2, 3, 4
  slotName: string; // Pasiva, Habilidad 1, Habilidad 2, Habilidad 3, Definitiva
  name: string;
  namePt?: string;
  description: string;
  descriptionPt?: string;
  cooldown?: string;
}

export interface Champion {
  id: string;
  name: string;
  title: string;
  ddragonId: string;
  avatarUrl: string;
  primaryRole: Role;
  secondaryRoles?: Role[];
  tier: string;
  tierMovement?: number;
  winrate: number;
  pickRate: number;
  banRate: number;
  damageType: DamageType;
  summary: string;
  advantageAgainst: string[];
  counteredBy: string[];
  synergies: string[];
  tacticalAdvice: string;
  recommendedRunes: string;
  runeTreeDetails: string;
  recommendedSpells: string[];
  coreItems: string[];
  situationalItems: string[];
  skillOrder?: string;
  skills?: ChampionSkill[];
  isRanged?: boolean;
  isFrontline?: boolean;
}

export interface DraftSlot {
  role: Role;
  champion: Champion | null;
}

export type Language = 'es' | 'pt';
