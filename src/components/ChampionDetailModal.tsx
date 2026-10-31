import React from 'react';
import { Champion, Language } from '../types';
import { translations, roleLabels } from '../utils/i18n';
import { X, ShieldAlert, Award, Crosshair, Sparkles, BookOpen } from 'lucide-react';

interface ChampionDetailModalProps {
  champion: Champion | null;
  onClose: () => void;
  lang: Language;
}

export const ChampionDetailModal: React.FC<ChampionDetailModalProps> = ({
  champion,
  onClose,
  lang,
}) => {
  if (!champion) return null;
  const t = translations[lang];
  const roleInfo = roleLabels[champion.primaryRole][lang];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in overflow-y-auto">
      <div className="relative w-full max-w-3xl bg-[#091428] border border-amber-500/40 rounded-2xl shadow-2xl overflow-hidden my-8 max-h-[90vh] flex flex-col">
        {/* Header Banner */}
        <div className="relative bg-gradient-to-r from-slate-900 via-[#0a1b33] to-slate-900 p-6 border-b border-slate-800 flex items-start justify-between">
          <div className="flex items-center space-x-4">
            <div className="relative w-20 h-20 rounded-xl overflow-hidden border-2 border-amber-500/60 shadow-lg shrink-0">
              <img
                src={`/champions/${champion.id}.png`}
                alt={champion.name}
                className="w-full h-full object-cover"
                onError={(e) => {
                  (e.target as HTMLImageElement).src = 'https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/29.jpg';
                }}
              />
              <span className="absolute bottom-0 right-0 bg-amber-500 text-slate-950 text-[10px] font-black px-1.5 py-0.5 rounded-tl">
                {champion.tier}
              </span>
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h2 className="text-2xl font-black text-white tracking-wide">{champion.name}</h2>
                <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-slate-800 border border-slate-700 text-amber-300">
                  {roleInfo}
                </span>
                <span className={`text-[11px] font-semibold px-2 py-0.5 rounded ${
                  champion.damageType === 'PHYSICAL'
                    ? 'bg-red-500/10 text-red-300 border border-red-500/20'
                    : champion.damageType === 'MAGIC'
                    ? 'bg-blue-500/10 text-blue-300 border border-blue-500/20'
                    : 'bg-purple-500/10 text-purple-300 border border-purple-500/20'
                }`}>
                  {champion.damageType === 'PHYSICAL' ? t.physical : champion.damageType === 'MAGIC' ? t.magic : t.trueDamage}
                </span>
              </div>
              <p className="text-xs text-amber-200/70 italic mt-0.5">{champion.title}</p>
              <div className="flex items-center space-x-4 mt-2 text-xs text-gray-300">
                <span>{t.winrate}: <strong className="text-emerald-400 font-bold">{champion.winrate}%</strong></span>
                <span>{t.pickrate}: <strong className="text-blue-400 font-bold">{champion.pickRate}%</strong></span>
                <span>{t.banrate}: <strong className="text-rose-400 font-bold">{champion.banRate}%</strong></span>
              </div>
            </div>
          </div>
          <button
            onClick={onClose}
            data-testid="modal_close_btn"
            className="p-2 text-gray-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 overflow-y-auto space-y-6 flex-1 text-sm">
          {/* Sovereign Tactical Advice */}
          <div className="bg-gradient-to-br from-amber-500/10 to-yellow-500/5 border border-amber-500/30 rounded-xl p-4">
            <div className="flex items-center space-x-2 text-amber-300 font-bold mb-2">
              <Award className="w-4 h-4 text-amber-400" />
              <span>Instrucción Táctica Soberano</span>
            </div>
            <p className="text-gray-200 text-xs sm:text-sm leading-relaxed whitespace-pre-line">
              {champion.tacticalAdvice || champion.summary}
            </p>
          </div>

          {/* Skill Order (Strict H1, H2, H3, H4/Ulti) */}
          <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4">
            <div className="flex items-center space-x-2 text-blue-300 font-bold mb-2">
              <Sparkles className="w-4 h-4 text-blue-400" />
              <span>{t.skillOrder}</span>
            </div>
            <div className="flex flex-wrap items-center gap-2">
              <span className="px-3 py-1 bg-slate-800 text-amber-300 font-mono font-bold text-xs rounded border border-slate-700">
                {champion.skillOrder || 'H1 > H3 > H2 > H4/Ulti'}
              </span>
              <span className="text-xs text-gray-400">
                Prioriza maximizar H4/Ulti en niveles 5, 9 y 13.
              </span>
            </div>
          </div>

          {/* Core Build & Situational Items */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4">
              <div className="flex items-center space-x-2 text-emerald-300 font-bold mb-3">
                <BookOpen className="w-4 h-4 text-emerald-400" />
                <span>{t.coreBuild}</span>
              </div>
              <ul className="space-y-2">
                {champion.coreItems.map((item, idx) => (
                  <li key={idx} className="flex items-center space-x-2 text-xs bg-slate-800/60 px-2.5 py-1.5 rounded border border-slate-700/50">
                    <span className="w-4 h-4 rounded-full bg-emerald-500/20 text-emerald-300 font-bold text-[10px] flex items-center justify-center">
                      {idx + 1}
                    </span>
                    <span className="text-gray-200 font-medium">{item}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4">
              <div className="flex items-center space-x-2 text-purple-300 font-bold mb-3">
                <Crosshair className="w-4 h-4 text-purple-400" />
                <span>{t.situationalBuild}</span>
              </div>
              <ul className="space-y-2">
                {champion.situationalItems.slice(0, 5).map((item, idx) => (
                  <li key={idx} className="flex items-center space-x-2 text-xs bg-slate-800/60 px-2.5 py-1.5 rounded border border-slate-700/50">
                    <span className="w-1.5 h-1.5 rounded-full bg-purple-400"></span>
                    <span className="text-gray-300">{item}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Runes & Spells */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4">
              <h4 className="text-xs font-bold text-amber-300 uppercase tracking-wider mb-2">{t.runes}</h4>
              <p className="text-xs text-gray-300 leading-relaxed font-mono">
                {champion.recommendedRunes}
              </p>
            </div>
            <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4">
              <h4 className="text-xs font-bold text-amber-300 uppercase tracking-wider mb-2">{t.spells}</h4>
              <div className="flex gap-2">
                {champion.recommendedSpells.map((spell, i) => (
                  <span key={i} className="px-2.5 py-1 bg-slate-800 border border-slate-700 rounded text-xs text-amber-200 font-medium">
                    {spell}
                  </span>
                ))}
              </div>
            </div>
          </div>

          {/* Matchups: Strong vs, Counters & Synergies */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div className="bg-slate-900/50 p-3 rounded-lg border border-slate-800">
              <span className="text-[11px] font-bold text-emerald-400 uppercase tracking-wide block mb-1">
                {t.advantages}
              </span>
              <div className="flex flex-wrap gap-1">
                {champion.advantageAgainst.map((adv, i) => (
                  <span key={i} className="text-xs bg-emerald-950/40 text-emerald-300 px-2 py-0.5 rounded border border-emerald-800/50">
                    {adv}
                  </span>
                ))}
              </div>
            </div>

            <div className="bg-slate-900/50 p-3 rounded-lg border border-slate-800">
              <span className="text-[11px] font-bold text-rose-400 uppercase tracking-wide block mb-1">
                {t.counters}
              </span>
              <div className="flex flex-wrap gap-1">
                {champion.counteredBy.map((cnt, i) => (
                  <span key={i} className="text-xs bg-rose-950/40 text-rose-300 px-2 py-0.5 rounded border border-rose-800/50">
                    {cnt}
                  </span>
                ))}
              </div>
            </div>

            <div className="bg-slate-900/50 p-3 rounded-lg border border-slate-800">
              <span className="text-[11px] font-bold text-blue-400 uppercase tracking-wide block mb-1">
                {t.synergiesWith}
              </span>
              <div className="flex flex-wrap gap-1">
                {champion.synergies.map((syn, i) => (
                  <span key={i} className="text-xs bg-blue-950/40 text-blue-300 px-2 py-0.5 rounded border border-blue-800/50">
                    {syn}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
