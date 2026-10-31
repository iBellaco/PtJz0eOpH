import React, { useState } from 'react';
import { Champion, Language, Role } from '../types';
import { translations, roleLabels } from '../utils/i18n';
import { TrendingUp, TrendingDown, Minus, Filter, Sparkles } from 'lucide-react';

interface TierListScreenProps {
  champions: Champion[];
  lang: Language;
  onOpenChampionDetail: (champ: Champion) => void;
}

export const TierListScreen: React.FC<TierListScreenProps> = ({
  champions,
  lang,
  onOpenChampionDetail,
}) => {
  const t = translations[lang];
  const [selectedRole, setSelectedRole] = useState<string>('ALL');

  const filteredChampions = champions.filter((c) => {
    if (selectedRole === 'ALL') return true;
    return c.primaryRole === selectedRole || (c.secondaryRoles && c.secondaryRoles.includes(selectedRole as Role));
  });

  const tiers = ['S+', 'S', 'A', 'B'];

  const getTierColor = (tier: string) => {
    switch (tier) {
      case 'S+':
        return 'from-amber-500 to-yellow-600 text-slate-950 border-amber-400';
      case 'S':
        return 'from-purple-600 to-indigo-700 text-white border-purple-400';
      case 'A':
        return 'from-blue-600 to-cyan-700 text-white border-blue-400';
      default:
        return 'from-slate-700 to-slate-800 text-gray-200 border-slate-600';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header and Filter */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-[#091428] border border-slate-800 p-4 rounded-xl shadow-md">
        <div>
          <h1 className="text-xl font-black text-amber-300 tracking-wide flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-amber-400" />
            <span>Clasificación Meta Wild Rift</span>
          </h1>
          <p className="text-xs text-gray-400 mt-0.5">
            Rendimiento competitivo ponderado por tasa de victoria, presencia en draft y sinergia Soberano.
          </p>
        </div>

        {/* Lane Filters */}
        <div className="flex flex-wrap gap-1.5 bg-slate-900 p-1.5 rounded-lg border border-slate-800">
          {['ALL', 'TOP', 'JUNGLE', 'MID', 'ADC', 'SUPPORT'].map((r) => (
            <button
              key={r}
              onClick={() => setSelectedRole(r)}
              data-testid={`filter_${r}`}
              className={`px-3 py-1.5 rounded-md text-xs font-bold transition-all min-h-[36px] ${
                selectedRole === r
                  ? 'bg-gradient-to-r from-amber-500 to-yellow-500 text-slate-950 font-black shadow-md'
                  : 'text-gray-400 hover:text-white hover:bg-slate-800'
              }`}
            >
              {r === 'ALL' ? t.allRoles : roleLabels[r as Role].short}
            </button>
          ))}
        </div>
      </div>

      {/* Tier Sections */}
      <div className="space-y-4">
        {tiers.map((tier) => {
          const tierChamps = filteredChampions
            .filter((c) => c.tier === tier)
            .sort((a, b) => b.winrate - a.winrate);

          if (tierChamps.length === 0) return null;

          return (
            <div
              key={tier}
              className="bg-[#091428] border border-slate-800/90 rounded-2xl overflow-hidden shadow-lg"
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between px-5 py-3 bg-slate-900/80 border-b border-slate-800 gap-2">
                <div className="flex items-center space-x-3">
                  <div
                    className={`w-12 h-10 rounded-xl bg-gradient-to-br flex items-center justify-center font-black text-lg border shadow-md ${getTierColor(
                      tier
                    )}`}
                  >
                    {tier}
                  </div>
                  <div>
                    <h3 className="text-sm font-black text-white uppercase tracking-wider">
                      Nivel {tier}
                    </h3>
                    <span className="text-[11px] text-gray-400 font-medium">
                      {tierChamps.length} campeones analizados
                    </span>
                  </div>
                </div>
              </div>

              {/* Champion Cards Row */}
              <div className="p-4 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
                {tierChamps.map((champ) => (
                  <div
                    key={champ.id}
                    onClick={() => onOpenChampionDetail(champ)}
                    data-testid={`tier_champ_${champ.id}`}
                    className="group bg-slate-900/90 border border-slate-800 hover:border-amber-400/80 p-2.5 rounded-xl transition-all cursor-pointer hover:-translate-y-0.5 hover:shadow-lg flex flex-col justify-between"
                  >
                    <div className="flex items-start space-x-2.5">
                      <div className="relative w-12 h-12 rounded-lg overflow-hidden border border-slate-700 shrink-0">
                        <img
                          src={`/champions/${champ.id}.png`}
                          alt={champ.name}
                          className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                          onError={(e) => {
                            (e.target as HTMLImageElement).src =
                              'https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/29.jpg';
                          }}
                        />
                      </div>
                      <div className="overflow-hidden flex-1">
                        <h4 className="text-xs font-bold text-white truncate group-hover:text-amber-300 transition-colors">
                          {champ.name}
                        </h4>
                        <span className="text-[10px] text-gray-400 block">
                          {roleLabels[champ.primaryRole].short}
                        </span>
                        <div className="flex items-center space-x-1 mt-1">
                          {champ.tierMovement && champ.tierMovement > 0 ? (
                            <TrendingUp className="w-3 h-3 text-emerald-400" />
                          ) : champ.tierMovement && champ.tierMovement < 0 ? (
                            <TrendingDown className="w-3 h-3 text-rose-400" />
                          ) : (
                            <Minus className="w-3 h-3 text-gray-500" />
                          )}
                          <span className="text-[10px] font-bold text-emerald-400 font-mono">
                            {champ.winrate}%
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="mt-2 pt-2 border-t border-slate-800/80 flex items-center justify-between text-[10px] text-gray-400">
                      <span>Pick: {champ.pickRate}%</span>
                      <span>Ban: {champ.banRate}%</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
