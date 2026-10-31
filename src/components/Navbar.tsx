import React from 'react';
import { Language } from '../types';
import { translations } from '../utils/i18n';
import { Shield, BarChart3, Users, Clock, Globe } from 'lucide-react';

interface NavbarProps {
  currentTab: string;
  onTabChange: (tab: string) => void;
  lang: Language;
  onLangChange: (lang: Language) => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentTab,
  onTabChange,
  lang,
  onLangChange,
}) => {
  const t = translations[lang];

  const navItems = [
    { id: 'draft', label: t.navDraft, icon: Shield },
    { id: 'tierlist', label: t.navTierList, icon: BarChart3 },
    { id: 'catalog', label: t.navCatalog, icon: Users },
    { id: 'macro', label: t.navMacro, icon: Clock },
  ];

  return (
    <header className="sticky top-0 z-50 bg-[#091428]/95 backdrop-blur-md border-b border-[#1e282d] shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Brand */}
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => onTabChange('draft')}>
            <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-amber-400 to-amber-700 flex items-center justify-center shadow-md shadow-amber-900/30 border border-amber-300/40">
              <span className="text-xl font-black text-slate-950 tracking-tighter">WR</span>
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="text-xl font-extrabold tracking-wide text-transparent bg-clip-text bg-gradient-to-r from-amber-200 via-yellow-400 to-amber-500">
                  {t.appName}
                </span>
                <span className="text-xs px-2 py-0.5 rounded font-bold uppercase bg-amber-500/10 text-amber-300 border border-amber-500/30">
                  Soberano
                </span>
              </div>
              <p className="text-[10px] text-gray-400 font-medium tracking-wide">
                Wild Rift Analytics & Coach
              </p>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="flex space-x-1 sm:space-x-2">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = currentTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => onTabChange(item.id)}
                  data-testid={`nav_${item.id}`}
                  className={`flex items-center space-x-1.5 px-3 py-2 rounded-lg text-sm font-semibold transition-all min-h-[44px] ${
                    isActive
                      ? 'bg-gradient-to-r from-amber-500/20 to-yellow-500/10 text-amber-300 border border-amber-500/40 shadow-inner'
                      : 'text-gray-300 hover:text-white hover:bg-slate-800/60'
                  }`}
                >
                  <Icon className={`w-4 h-4 ${isActive ? 'text-amber-400' : 'text-gray-400'}`} />
                  <span className="hidden sm:inline">{item.label}</span>
                </button>
              );
            })}
          </nav>

          {/* Language Switcher */}
          <div className="flex items-center space-x-2">
            <button
              onClick={() => onLangChange(lang === 'es' ? 'pt' : 'es')}
              data-testid="lang_toggle"
              className="flex items-center space-x-1.5 px-2.5 py-1.5 rounded-lg text-xs font-bold bg-slate-800/80 hover:bg-slate-700 border border-slate-700 text-gray-200 transition-colors"
              title="Cambiar idioma / Mudar idioma"
            >
              <Globe className="w-3.5 h-3.5 text-amber-400" />
              <span>{lang.toUpperCase()}</span>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};
