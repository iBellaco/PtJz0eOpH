import React, { useState } from 'react';
import { Champion, Language, Role } from '../types';
import { translations, roleLabels } from '../utils/i18n';
import { Search, Sparkles, Shield, Zap } from 'lucide-react';

interface ChampionsCatalogScreenProps {
  champions: Champion[];
  lang: Language;
  onOpenChampionDetail: (champ: Champion) => void;
}

export const ChampionsCatalogScreen: React.FC<ChampionsCatalogScreenProps> = ({
  champions,
  lang,
  onOpenChampionDetail,
}) => {
  const t = translations[lang];
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [damageFilter, setDamageFilter] = useState('ALL');

  const filtered = champions.filter((champ) => {
    const matchesSearch =
      champ.name.toLowerCase().includes(search.toLowerCase()) ||
      champ.title.toLowerCase().includes(search.toLowerCase());
    const matchesRole =
      roleFilter === 'ALL' ||
      champ.primaryRole === roleFilter ||
      (champ.secondaryRoles && champ.secondaryRoles.includes(roleFilter as Role));
    const matchesDamage = damageFilter === 'ALL' || champ.damageType === damageFilter;
    return matchesSearch && matchesRole && matchesDamage;
  });

  return (
    <div className="space-y-6">
      {/* Header and Controls */}
      <div className="bg-[#091428] border border-slate-800 p-4 rounded-xl shadow-md space-y-4">
        <div>
          <h1 className="text-xl font-black text-amber-300 tracking-wide flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-amber-400" />
            <span>Catálogo Completo de Campeones</span>
          </h1>
          <p className="text-xs text-gray-400 mt-0.5">
            142 campeones con itemización Soberano, runas, hechizos, orden de habilidades H1-H4/Ulti y enfrentamientos directos.
          </p>
        </div>

        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-2.5 w-4 h-4 text-gray-400" />
            <input
              type="text"
              placeholder={t.searchChampion}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-lg text-sm text-white placeholder-gray-500 focus:outline-none focus:border-amber-500"
            />
          </div>

          <div className="flex flex-wrap gap-1.5">
            {['ALL', 'TOP', 'JUNGLE', 'MID', 'ADC', 'SUPPORT'].map((r) => (
              <button
                key={r}
                onClick={() => setRoleFilter(r)}
                className={`px-3 py-1.5 rounded-md text-xs font-bold transition-all ${
                  roleFilter === r
                    ? 'bg-amber-500 text-slate-950 font-black'
                    : 'bg-slate-800 text-gray-400 hover:text-white'
                }`}
              >
                {r === 'ALL' ? t.allRoles : roleLabels[r as Role].short}
              </button>
            ))}
          </div>

          <div className="flex gap-1.5">
            {[
              { id: 'ALL', label: 'Todo daño' },
              { id: 'PHYSICAL', label: 'Físico' },
              { id: 'MAGIC', label: 'Mágico' },
            ].map((d) => (
              <button
                key={d.id}
                onClick={() => setDamageFilter(d.id)}
                className={`px-2.5 py-1.5 rounded-md text-xs font-semibold ${
                  damageFilter === d.id
                    ? 'bg-blue-600 text-white'
                    : 'bg-slate-800 text-gray-400 hover:text-white'
                }`}
              >
                {d.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Champions Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
        {filtered.map((champ) => (
          <div
            key={champ.id}
            onClick={() => onOpenChampionDetail(champ)}
            data-testid={`catalog_champ_${champ.id}`}
            className="group bg-[#091428] border border-slate-800 hover:border-amber-400/80 p-3 rounded-xl transition-all cursor-pointer hover:-translate-y-1 hover:shadow-xl flex flex-col justify-between"
          >
            <div>
              <div className="relative w-full aspect-square rounded-lg overflow-hidden border border-slate-700/80 mb-2">
                <img
                  src={`/champions/${champ.id}.png`}
                  alt={champ.name}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  onError={(e) => {
                    (e.target as HTMLImageElement).src =
                      'https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/29.jpg';
                  }}
                />
                <span className="absolute top-1 right-1 bg-amber-500 text-slate-950 text-[10px] font-black px-1.5 py-0.2 rounded shadow">
                  {champ.tier}
                </span>
              </div>
              <h3 className="text-xs font-bold text-white group-hover:text-amber-300 transition-colors truncate">
                {champ.name}
              </h3>
              <p className="text-[10px] text-gray-400 truncate">{champ.title}</p>
            </div>

            <div className="mt-2 pt-2 border-t border-slate-800/80 flex items-center justify-between text-[10px]">
              <span className="text-amber-400 font-bold">{roleLabels[champ.primaryRole].short}</span>
              <span className="text-emerald-400 font-mono font-bold">{champ.winrate}%</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
