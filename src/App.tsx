import React, { useState } from 'react';
import { Champion, Language } from './types';
import rawChampions from './data/champions.json';
import { Navbar } from './components/Navbar';
import { DraftScreen } from './components/DraftScreen';
import { TierListScreen } from './components/TierListScreen';
import { ChampionsCatalogScreen } from './components/ChampionsCatalogScreen';
import { MacroObjectivesScreen } from './components/MacroObjectivesScreen';
import { ChampionDetailModal } from './components/ChampionDetailModal';

const champions = rawChampions as unknown as Champion[];

export const App: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<string>('draft');
  const [lang, setLang] = useState<Language>('es');
  const [detailChampion, setDetailChampion] = useState<Champion | null>(null);

  return (
    <div className="min-h-screen bg-[#050a12] text-gray-100 flex flex-col font-sans selection:bg-amber-500/30 selection:text-amber-200">
      {/* Top Navigation */}
      <Navbar
        currentTab={currentTab}
        onTabChange={setCurrentTab}
        lang={lang}
        onLangChange={setLang}
      />

      {/* Main Content Viewport */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {currentTab === 'draft' && (
          <DraftScreen
            champions={champions}
            lang={lang}
            onOpenChampionDetail={setDetailChampion}
          />
        )}

        {currentTab === 'tierlist' && (
          <TierListScreen
            champions={champions}
            lang={lang}
            onOpenChampionDetail={setDetailChampion}
          />
        )}

        {currentTab === 'catalog' && (
          <ChampionsCatalogScreen
            champions={champions}
            lang={lang}
            onOpenChampionDetail={setDetailChampion}
          />
        )}

        {currentTab === 'macro' && (
          <MacroObjectivesScreen lang={lang} />
        )}
      </main>

      {/* Detailed Champion Modal */}
      <ChampionDetailModal
        champion={detailChampion}
        onClose={() => setDetailChampion(null)}
        lang={lang}
      />

      {/* App Footer with Version info pinned at bottom right */}
      <footer className="bg-[#091428] border-t border-slate-800/80 py-3 px-4 sm:px-8 mt-auto">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between text-xs text-gray-400 gap-2">
          <div className="flex items-center space-x-2">
            <span className="font-bold text-amber-400">Coach</span>
            <span>•</span>
            <span>Wild Rift Competitive Decision Auditor</span>
          </div>

          {/* Mandatory Version display on the bottom right */}
          <div className="flex items-center space-x-3 text-[11px] font-mono text-gray-400" data-testid="app_version_label">
            <span>Sincronización en la nube activa</span>
            <span>•</span>
            <span className="text-amber-300 font-bold bg-amber-500/10 px-2 py-0.5 rounded border border-amber-500/30">
              v1.1.10.428 (Build 1144)
            </span>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default App;
