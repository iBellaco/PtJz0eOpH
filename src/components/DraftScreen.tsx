import React, { useState } from 'react';
import { Champion, DraftSlot, Language, Role } from '../types';
import { translations, roleLabels } from '../utils/i18n';
import { evaluateDraft } from '../utils/coachEvaluator';
import {
  Shield,
  Swords,
  RotateCcw,
  Sparkles,
  Zap,
  Flame,
  Search,
  CheckCircle2,
  AlertTriangle,
  Info
} from 'lucide-react';

interface DraftScreenProps {
  champions: Champion[];
  lang: Language;
  onOpenChampionDetail: (champ: Champion) => void;
}

export const DraftScreen: React.FC<DraftScreenProps> = ({
  champions,
  lang,
  onOpenChampionDetail,
}) => {
  const t = translations[lang];

  const defaultRoles: Role[] = ['TOP', 'JUNGLE', 'MID', 'ADC', 'SUPPORT'];

  const [allySlots, setAllySlots] = useState<DraftSlot[]>(
    defaultRoles.map((role) => ({ role, champion: null }))
  );
  const [enemySlots, setEnemySlots] = useState<DraftSlot[]>(
    defaultRoles.map((role) => ({ role, champion: null }))
  );
  const [allyBans, setAllyBans] = useState<(Champion | null)[]>([null, null, null, null, null]);
  const [enemyBans, setEnemyBans] = useState<(Champion | null)[]>([null, null, null, null, null]);

  // Picker modal state
  const [pickerTarget, setPickerTarget] = useState<{
    side: 'ally' | 'enemy' | 'allyBan' | 'enemyBan';
    index: number;
    role?: Role;
  } | null>(null);

  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState<string>('ALL');

  const audit = evaluateDraft(allySlots, enemySlots, lang);

  // Selected champion IDs to avoid duplicate picking
  const pickedIds = new Set<string>();
  allySlots.forEach((s) => s.champion && pickedIds.add(s.champion.id));
  enemySlots.forEach((s) => s.champion && pickedIds.add(s.champion.id));
  allyBans.forEach((b) => b && pickedIds.add(b.id));
  enemyBans.forEach((b) => b && pickedIds.add(b.id));

  const handleSelectChampion = (champion: Champion) => {
    if (!pickerTarget) return;

    if (pickerTarget.side === 'ally') {
      const updated = [...allySlots];
      updated[pickerTarget.index] = { ...updated[pickerTarget.index], champion };
      setAllySlots(updated);
    } else if (pickerTarget.side === 'enemy') {
      const updated = [...enemySlots];
      updated[pickerTarget.index] = { ...updated[pickerTarget.index], champion };
      setEnemySlots(updated);
    } else if (pickerTarget.side === 'allyBan') {
      const updated = [...allyBans];
      updated[pickerTarget.index] = champion;
      setAllyBans(updated);
    } else if (pickerTarget.side === 'enemyBan') {
      const updated = [...enemyBans];
      updated[pickerTarget.index] = champion;
      setEnemyBans(updated);
    }

    setPickerTarget(null);
    setSearchQuery('');
  };

  const handleRemoveChampion = (side: 'ally' | 'enemy' | 'allyBan' | 'enemyBan', index: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (side === 'ally') {
      const updated = [...allySlots];
      updated[index] = { ...updated[index], champion: null };
      setAllySlots(updated);
    } else if (side === 'enemy') {
      const updated = [...enemySlots];
      updated[index] = { ...updated[index], champion: null };
      setEnemySlots(updated);
    } else if (side === 'allyBan') {
      const updated = [...allyBans];
      updated[index] = null;
      setAllyBans(updated);
    } else if (side === 'enemyBan') {
      const updated = [...enemyBans];
      updated[index] = null;
      setEnemyBans(updated);
    }
  };

  const handleClearAll = () => {
    setAllySlots(defaultRoles.map((role) => ({ role, champion: null })));
    setEnemySlots(defaultRoles.map((role) => ({ role, champion: null })));
    setAllyBans([null, null, null, null, null]);
    setEnemyBans([null, null, null, null, null]);
  };

  // Filter champions for picker
  const filteredChampions = champions.filter((c) => {
    const matchesSearch = c.name.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesRole =
      roleFilter === 'ALL' ||
      c.primaryRole === roleFilter ||
      (c.secondaryRoles && c.secondaryRoles.includes(roleFilter as Role));
    return matchesSearch && matchesRole;
  });

  return (
    <div className="space-y-6">
      {/* Top Banner / Actions */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-[#091428] border border-slate-800 p-4 rounded-xl shadow-md">
        <div>
          <h1 className="text-xl font-black text-amber-300 tracking-wide flex items-center gap-2">
            <Swords className="w-5 h-5 text-amber-400" />
            <span>Auditoría de Selección y Macro</span>
          </h1>
          <p className="text-xs text-gray-400 mt-0.5">
            Diagnóstico táctico en tiempo real: sinergias, perfiles de daño, micro y condiciones de victoria.
          </p>
        </div>
        <button
          onClick={handleClearAll}
          data-testid="clear_draft_btn"
          className="flex items-center space-x-1.5 px-3 py-2 bg-slate-800 hover:bg-slate-700 text-gray-300 text-xs font-bold rounded-lg border border-slate-700 transition-colors"
        >
          <RotateCcw className="w-3.5 h-3.5" />
          <span>{t.clearDraft}</span>
        </button>
      </div>

      {/* Draft Grid: Ally Team vs Enemy Team */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Ally Team Panel */}
        <div className="bg-[#091428] border border-blue-500/30 rounded-xl p-4 shadow-lg space-y-4">
          <div className="flex items-center justify-between border-b border-blue-500/20 pb-2">
            <h2 className="text-sm font-black text-blue-400 tracking-wider uppercase flex items-center gap-1.5">
              <Shield className="w-4 h-4 text-blue-400" />
              <span>{t.allyTeam}</span>
            </h2>
            <span className="text-[11px] font-bold text-gray-400">
              {allySlots.filter((s) => s.champion).length} / 5
            </span>
          </div>

          <div className="space-y-2">
            {allySlots.map((slot, idx) => {
              const roleName = roleLabels[slot.role][lang];
              const champ = slot.champion;
              return (
                <div
                  key={idx}
                  onClick={() => setPickerTarget({ side: 'ally', index: idx, role: slot.role })}
                  data-testid={`ally_slot_${idx}`}
                  className={`flex items-center justify-between p-2.5 rounded-lg border transition-all cursor-pointer min-h-[58px] ${
                    champ
                      ? 'bg-slate-900/90 border-blue-500/40 hover:border-blue-400'
                      : 'bg-slate-900/40 border-slate-800 border-dashed hover:border-blue-500/30'
                  }`}
                >
                  <div className="flex items-center space-x-3">
                    <span className="w-14 text-[11px] font-black uppercase text-blue-300/80 tracking-wider">
                      {roleLabels[slot.role].short}
                    </span>
                    {champ ? (
                      <div className="flex items-center space-x-3">
                        <img
                          src={`/champions/${champ.id}.png`}
                          alt={champ.name}
                          className="w-10 h-10 rounded-lg object-cover border border-blue-400/50"
                          onError={(e) => {
                            (e.target as HTMLImageElement).src = 'https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/29.jpg';
                          }}
                        />
                        <div>
                          <div className="flex items-center space-x-2">
                            <span className="text-sm font-bold text-white">{champ.name}</span>
                            <span className="text-[10px] px-1.5 py-0.2 bg-amber-500/20 text-amber-300 rounded font-black border border-amber-500/30">
                              {champ.tier}
                            </span>
                          </div>
                          <span className="text-[10px] text-gray-400">
                            WR: {champ.winrate}% • {champ.damageType === 'PHYSICAL' ? t.physical : champ.damageType === 'MAGIC' ? t.magic : t.trueDamage}
                          </span>
                        </div>
                      </div>
                    ) : (
                      <span className="text-xs text-gray-500 italic font-medium">
                        {t.slotEmpty} ({roleName})
                      </span>
                    )}
                  </div>

                  {champ && (
                    <div className="flex items-center space-x-2">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onOpenChampionDetail(champ);
                        }}
                        className="p-1.5 bg-slate-800 hover:bg-slate-700 text-amber-300 rounded text-xs font-semibold"
                        title="Ver Guía Táctica Soberano"
                      >
                        <Info className="w-4 h-4" />
                      </button>
                      <button
                        onClick={(e) => handleRemoveChampion('ally', idx, e)}
                        className="text-gray-500 hover:text-rose-400 p-1 text-xs"
                      >
                        ✕
                      </button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>

          {/* Ally Bans */}
          <div className="pt-2 border-t border-slate-800">
            <span className="text-[11px] font-bold text-gray-400 block mb-2">{t.bans} Aliados</span>
            <div className="flex gap-2">
              {allyBans.map((ban, idx) => (
                <div
                  key={idx}
                  onClick={() => setPickerTarget({ side: 'allyBan', index: idx })}
                  className="w-10 h-10 rounded-lg bg-slate-900 border border-slate-800 flex items-center justify-center cursor-pointer relative overflow-hidden group hover:border-rose-500/50"
                >
                  {ban ? (
                    <>
                      <img src={`/champions/${ban.id}.png`} alt={ban.name} className="w-full h-full object-cover opacity-60 grayscale" />
                      <div className="absolute inset-0 bg-rose-950/60 flex items-center justify-center">
                        <span className="text-rose-400 text-xs font-black">🚫</span>
                      </div>
                    </>
                  ) : (
                    <span className="text-[10px] text-gray-600 font-bold">B{idx + 1}</span>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Enemy Team Panel */}
        <div className="bg-[#091428] border border-rose-500/30 rounded-xl p-4 shadow-lg space-y-4">
          <div className="flex items-center justify-between border-b border-rose-500/20 pb-2">
            <h2 className="text-sm font-black text-rose-400 tracking-wider uppercase flex items-center gap-1.5">
              <Swords className="w-4 h-4 text-rose-400" />
              <span>{t.enemyTeam}</span>
            </h2>
            <span className="text-[11px] font-bold text-gray-400">
              {enemySlots.filter((s) => s.champion).length} / 5
            </span>
          </div>

          <div className="space-y-2">
            {enemySlots.map((slot, idx) => {
              const roleName = roleLabels[slot.role][lang];
              const champ = slot.champion;
              return (
                <div
                  key={idx}
                  onClick={() => setPickerTarget({ side: 'enemy', index: idx, role: slot.role })}
                  data-testid={`enemy_slot_${idx}`}
                  className={`flex items-center justify-between p-2.5 rounded-lg border transition-all cursor-pointer min-h-[58px] ${
                    champ
                      ? 'bg-slate-900/90 border-rose-500/40 hover:border-rose-400'
                      : 'bg-slate-900/40 border-slate-800 border-dashed hover:border-rose-500/30'
                  }`}
                >
                  <div className="flex items-center space-x-3">
                    <span className="w-14 text-[11px] font-black uppercase text-rose-300/80 tracking-wider">
                      {roleLabels[slot.role].short}
                    </span>
                    {champ ? (
                      <div className="flex items-center space-x-3">
                        <img
                          src={`/champions/${champ.id}.png`}
                          alt={champ.name}
                          className="w-10 h-10 rounded-lg object-cover border border-rose-400/50"
                          onError={(e) => {
                            (e.target as HTMLImageElement).src = 'https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/29.jpg';
                          }}
                        />
                        <div>
                          <div className="flex items-center space-x-2">
                            <span className="text-sm font-bold text-white">{champ.name}</span>
                            <span className="text-[10px] px-1.5 py-0.2 bg-amber-500/20 text-amber-300 rounded font-black border border-amber-500/30">
                              {champ.tier}
                            </span>
                          </div>
                          <span className="text-[10px] text-gray-400">
                            WR: {champ.winrate}% • {champ.damageType === 'PHYSICAL' ? t.physical : champ.damageType === 'MAGIC' ? t.magic : t.trueDamage}
                          </span>
                        </div>
                      </div>
                    ) : (
                      <span className="text-xs text-gray-500 italic font-medium">
                        {t.slotEmpty} ({roleName})
                      </span>
                    )}
                  </div>

                  {champ && (
                    <div className="flex items-center space-x-2">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onOpenChampionDetail(champ);
                        }}
                        className="p-1.5 bg-slate-800 hover:bg-slate-700 text-amber-300 rounded text-xs font-semibold"
                        title="Ver Guía Táctica Soberano"
                      >
                        <Info className="w-4 h-4" />
                      </button>
                      <button
                        onClick={(e) => handleRemoveChampion('enemy', idx, e)}
                        className="text-gray-500 hover:text-rose-400 p-1 text-xs"
                      >
                        ✕
                      </button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>

          {/* Enemy Bans */}
          <div className="pt-2 border-t border-slate-800">
            <span className="text-[11px] font-bold text-gray-400 block mb-2">{t.bans} Rivales</span>
            <div className="flex gap-2">
              {enemyBans.map((ban, idx) => (
                <div
                  key={idx}
                  onClick={() => setPickerTarget({ side: 'enemyBan', index: idx })}
                  className="w-10 h-10 rounded-lg bg-slate-900 border border-slate-800 flex items-center justify-center cursor-pointer relative overflow-hidden group hover:border-rose-500/50"
                >
                  {ban ? (
                    <>
                      <img src={`/champions/${ban.id}.png`} alt={ban.name} className="w-full h-full object-cover opacity-60 grayscale" />
                      <div className="absolute inset-0 bg-rose-950/60 flex items-center justify-center">
                        <span className="text-rose-400 text-xs font-black">🚫</span>
                      </div>
                    </>
                  ) : (
                    <span className="text-[10px] text-gray-600 font-bold">B{idx + 1}</span>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Metrics Bar */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-[#091428] border border-slate-800 p-3.5 rounded-xl">
          <span className="text-[11px] font-bold text-gray-400 block uppercase">{t.synergyScore}</span>
          <div className="flex items-center space-x-2 mt-1">
            <div className="text-xl font-black text-amber-300">{audit.synergyRating}%</div>
            <div className="flex-1 bg-slate-800 h-2 rounded-full overflow-hidden">
              <div className="bg-amber-400 h-full rounded-full transition-all" style={{ width: `${audit.synergyRating}%` }} />
            </div>
          </div>
        </div>

        <div className="bg-[#091428] border border-slate-800 p-3.5 rounded-xl">
          <span className="text-[11px] font-bold text-gray-400 block uppercase">{t.damageComp}</span>
          <div className="text-xs font-semibold text-gray-300 mt-1 flex justify-between">
            <span className="text-red-400 font-bold">{audit.damageDistribution.physical}% Fís</span>
            <span className="text-blue-400 font-bold">{audit.damageDistribution.magic}% Mág</span>
            <span className="text-purple-400 font-bold">{audit.damageDistribution.trueDamage}% Híb</span>
          </div>
        </div>

        <div className="bg-[#091428] border border-slate-800 p-3.5 rounded-xl">
          <span className="text-[11px] font-bold text-gray-400 block uppercase">{t.crowdControl}</span>
          <div className="flex items-center space-x-2 mt-1">
            <div className="text-xl font-black text-cyan-300">{audit.crowdControlScore}%</div>
            <div className="flex-1 bg-slate-800 h-2 rounded-full overflow-hidden">
              <div className="bg-cyan-400 h-full rounded-full transition-all" style={{ width: `${audit.crowdControlScore}%` }} />
            </div>
          </div>
        </div>

        <div className="bg-[#091428] border border-slate-800 p-3.5 rounded-xl">
          <span className="text-[11px] font-bold text-gray-400 block uppercase">{t.frontline}</span>
          <div className="flex items-center space-x-2 mt-1">
            <div className="text-xl font-black text-emerald-300">{audit.frontlineScore}%</div>
            <div className="flex-1 bg-slate-800 h-2 rounded-full overflow-hidden">
              <div className="bg-emerald-400 h-full rounded-full transition-all" style={{ width: `${audit.frontlineScore}%` }} />
            </div>
          </div>
        </div>
      </div>

      {/* Flagship Sovereign Coach 4-Step Analysis Card */}
      <div className="bg-gradient-to-br from-[#0c1e38] via-[#091428] to-[#070e1c] border-2 border-amber-500/50 rounded-2xl p-6 shadow-2xl relative overflow-hidden">
        <div className="flex items-center justify-between border-b border-amber-500/30 pb-3 mb-5">
          <div className="flex items-center space-x-2.5">
            <div className="w-8 h-8 rounded-lg bg-amber-500/20 border border-amber-500/40 flex items-center justify-center">
              <Sparkles className="w-5 h-5 text-amber-300" />
            </div>
            <div>
              <h3 className="text-base font-black text-amber-300 uppercase tracking-wide">
                {t.coachAnalysis}
              </h3>
              <p className="text-[11px] text-gray-400 font-medium">
                Criterio competitivo de nivel Soberano para el cierre y ejecución de partida.
              </p>
            </div>
          </div>
          <span className="text-xs font-mono font-bold px-2.5 py-1 rounded-md bg-amber-500/10 text-amber-300 border border-amber-500/30">
            AUDITORÍA EN VIVO
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-5 text-xs sm:text-sm">
          {/* Step 1: Diagnóstico */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-xl p-4 space-y-1.5 shadow-sm">
            <div className="flex items-center space-x-2 text-rose-400 font-extrabold text-xs uppercase tracking-wider">
              <AlertTriangle className="w-4 h-4 shrink-0 text-rose-400" />
              <span>{t.diagTitle}</span>
            </div>
            <p className="text-gray-200 leading-relaxed pl-6 text-xs sm:text-sm">
              {audit.diagnostic}
            </p>
          </div>

          {/* Step 2: Decisión Soberano */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-xl p-4 space-y-1.5 shadow-sm">
            <div className="flex items-center space-x-2 text-amber-400 font-extrabold text-xs uppercase tracking-wider">
              <CheckCircle2 className="w-4 h-4 shrink-0 text-amber-400" />
              <span>{t.soberanoTitle}</span>
            </div>
            <p className="text-gray-200 leading-relaxed pl-6 text-xs sm:text-sm">
              {audit.sovereignDecision}
            </p>
          </div>

          {/* Step 3: Micro y Macro detalle */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-xl p-4 space-y-1.5 shadow-sm">
            <div className="flex items-center space-x-2 text-cyan-400 font-extrabold text-xs uppercase tracking-wider">
              <Zap className="w-4 h-4 shrink-0 text-cyan-400" />
              <span>{t.microMacroTitle}</span>
            </div>
            <p className="text-gray-200 leading-relaxed pl-6 text-xs sm:text-sm">
              {audit.microMacroDetail}
            </p>
          </div>

          {/* Step 4: Regla aplicable */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-xl p-4 space-y-1.5 shadow-sm">
            <div className="flex items-center space-x-2 text-emerald-400 font-extrabold text-xs uppercase tracking-wider">
              <Flame className="w-4 h-4 shrink-0 text-emerald-400" />
              <span>{t.ruleTitle}</span>
            </div>
            <p className="text-gray-200 leading-relaxed pl-6 text-xs sm:text-sm font-medium">
              {audit.applicableRule}
            </p>
          </div>
        </div>

        {audit.keyAdvice.length > 0 && (
          <div className="mt-4 pt-3 border-t border-slate-800/80">
            <ul className="space-y-1">
              {audit.keyAdvice.map((adv, i) => (
                <li key={i} className="text-xs text-amber-200/90 flex items-center space-x-2">
                  <span className="w-1.5 h-1.5 rounded-full bg-amber-400"></span>
                  <span>{adv}</span>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {/* Champion Picker Modal */}
      {pickerTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md">
          <div className="relative w-full max-w-2xl bg-[#091428] border border-amber-500/50 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[85vh]">
            <div className="p-4 border-b border-slate-800 flex items-center justify-between">
              <div>
                <h3 className="text-base font-bold text-white">
                  {t.selectForRole}{' '}
                  <span className="text-amber-400">
                    {pickerTarget.role ? roleLabels[pickerTarget.role][lang] : 'Baneo'}
                  </span>
                </h3>
                <p className="text-xs text-gray-400">
                  {pickerTarget.side.includes('ally') ? t.allyTeam : t.enemyTeam}
                </p>
              </div>
              <button
                onClick={() => setPickerTarget(null)}
                className="p-1.5 text-gray-400 hover:text-white rounded-lg hover:bg-slate-800"
              >
                ✕
              </button>
            </div>

            {/* Search and Filters */}
            <div className="p-4 border-b border-slate-800/80 space-y-3 bg-slate-900/60">
              <div className="relative">
                <Search className="absolute left-3 top-2.5 w-4 h-4 text-gray-400" />
                <input
                  type="text"
                  placeholder={t.searchChampion}
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-9 pr-4 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white placeholder-gray-500 focus:outline-none focus:border-amber-500"
                  autoFocus
                />
              </div>

              <div className="flex flex-wrap gap-1.5">
                {['ALL', 'TOP', 'JUNGLE', 'MID', 'ADC', 'SUPPORT'].map((r) => (
                  <button
                    key={r}
                    onClick={() => setRoleFilter(r)}
                    className={`px-2.5 py-1 rounded text-xs font-bold transition-colors ${
                      roleFilter === r
                        ? 'bg-amber-500 text-slate-950 font-black'
                        : 'bg-slate-800 text-gray-400 hover:text-white'
                    }`}
                  >
                    {r === 'ALL' ? t.allRoles : roleLabels[r as Role].short}
                  </button>
                ))}
              </div>
            </div>

            {/* Champions Grid */}
            <div className="p-4 overflow-y-auto grid grid-cols-4 sm:grid-cols-6 gap-3 flex-1">
              {filteredChampions.map((champ) => {
                const isPicked = pickedIds.has(champ.id);
                return (
                  <button
                    key={champ.id}
                    disabled={isPicked}
                    onClick={() => handleSelectChampion(champ)}
                    className={`flex flex-col items-center p-2 rounded-xl border text-center transition-all ${
                      isPicked
                        ? 'opacity-30 border-transparent cursor-not-allowed bg-slate-900/40'
                        : 'bg-slate-900/80 border-slate-800 hover:border-amber-400 hover:scale-105'
                    }`}
                  >
                    <div className="relative w-12 h-12 rounded-lg overflow-hidden border border-slate-700 mb-1.5">
                      <img
                        src={`/champions/${champ.id}.png`}
                        alt={champ.name}
                        className="w-full h-full object-cover"
                        onError={(e) => {
                          (e.target as HTMLImageElement).src = 'https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/29.jpg';
                        }}
                      />
                    </div>
                    <span className="text-[11px] font-bold text-gray-200 truncate w-full">
                      {champ.name}
                    </span>
                    <span className="text-[9px] text-amber-400 font-black font-mono">
                      {champ.tier} • {champ.winrate}%
                    </span>
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
