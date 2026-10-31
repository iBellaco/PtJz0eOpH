import React from 'react';
import { Language } from '../types';
import { translations } from '../utils/i18n';
import { Clock, Shield, Award, Zap, Crosshair, ArrowRight } from 'lucide-react';

interface MacroObjectivesScreenProps {
  lang: Language;
}

export const MacroObjectivesScreen: React.FC<MacroObjectivesScreenProps> = ({ lang }) => {
  const isEs = lang === 'es';

  const objectives = [
    {
      time: '1:25',
      name: isEs ? 'Escarabajo de la Grieta' : 'Aronguejo do Rio',
      lane: 'Río Superior / Inferior',
      desc: isEs
        ? 'Asegurar prioridad en línea al 1:15 mediante empuje rápido (push) para asistir al jungla. El escudo del escarabajo se rompe instantáneamente con control de masas duro.'
        : 'Garantir prioridade na rota aos 1:15 com avanço rápido para apoiar o caçador. O escudo é quebrado instantaneamente com controle de grupo rígido.',
      color: 'border-emerald-500/40 text-emerald-400',
    },
    {
      time: '5:00',
      name: isEs ? 'Dragón Elemental' : 'Dragão Elemental',
      lane: 'Foso Inferior de Dragón',
      desc: isEs
        ? 'Base tempo obligatoria a los 4:15 para gastar oro y comprar componentes. Crashear la oleada de bot/mid a los 4:45 para obligar al rival a elegir entre súbditos o dragón.'
        : 'Retorno à base obrigatório aos 4:15 para gastar ouro e comprar componentes. Colidir a leva do duo/meio aos 4:45 para forçar o rival a escolher entre tropas ou dragão.',
      color: 'border-amber-500/40 text-amber-400',
    },
    {
      time: '5:00',
      name: isEs ? 'Heraldo de la Grieta' : 'Arauto do Vale',
      lane: 'Foso Superior',
      desc: isEs
        ? 'Objetivo de mayor valor de oro temprano. Úsalo para derribar la primera torre antes del minuto 7:30 y cobrar las placas de torreta.'
        : 'Objetivo de maior valor de ouro inicial. Use para derrubar a primeira torre antes dos 7:30 e garantir as barricadas.',
      color: 'border-purple-500/40 text-purple-400',
    },
    {
      time: '7:30',
      name: isEs ? 'Caída de Placas de Torreta' : 'Queda das Barricadas',
      lane: 'Todas las torres exteriores',
      desc: isEs
        ? 'Las placas desaparecen y las torretas pierden su resistencia pasiva. El juego pasa oficialmente de fase de líneas a rotaciones de juego medio.'
        : 'As barricadas desaparecem e as torres perdem a resistência passiva. O jogo passa formalmente para rotações de meio de jogo.',
      color: 'border-cyan-500/40 text-cyan-400',
    },
    {
      time: '12:00',
      name: isEs ? 'Barón Nashor' : 'Barão Nashor',
      lane: 'Foso Superior de Barón',
      desc: isEs
        ? 'Buff decisivo para asedio de torres. Requiere que un empujador lateral cree presión 1-3-1 en la línea opuesta para forzar una respuesta en desventaja.'
        : 'Bônus decisivo para cerco a torres. Exige que um avanço lateral pressione no 1-3-1 na rota oposta para forçar resposta desvantajosa.',
      color: 'border-rose-500/40 text-rose-400',
    },
    {
      time: '18:00',
      name: isEs ? 'Dragón Anciano' : 'Dragão Ancião',
      lane: 'Foso de Dragón',
      desc: isEs
        ? 'Condición de victoria definitiva. Otorga quemadura verdadera y ejecución instantánea al alcanzar menos del 20% de vida máxima.'
        : 'Condição de vitória definitiva. Concede queimadura verdadeira e execução instantânea ao atingir menos de 20% de vida máxima.',
      color: 'border-yellow-400/50 text-yellow-300',
    },
  ];

  const waveRules = [
    {
      title: isEs ? 'Slow Push (Empuje Lento)' : 'Slow Push (Avanço Lento)',
      condition: isEs ? 'Matar solo los súbditos magos/tiradores' : 'Eliminar apenas as tropas magas/atiradoras',
      result: isEs
        ? 'Tus súbditos se acumulan formando una ola masiva de 2 a 3 oleadas que colisiona bajo la torre rival justo al momento del objetivo neutral.'
        : 'Suas tropas acumulam formando uma grande onda de 2 a 3 levas que colide sob a torre rival no momento do objetivo neutro.',
    },
    {
      title: isEs ? 'Freeze (Congelación)' : 'Freeze (Congelamento)',
      condition: isEs ? 'Mantener 3-4 súbditos magos rivales vivos fuera del rango de tu torre' : 'Manter 3-4 tropas magas vivas fora do alcance da torre',
      result: isEs
        ? 'El rival queda desprotegido en mitad del río, forzado a exponerse a gankeos o perder experiencia y oro de último golpe.'
        : 'O rival fica desprotegido no meio do rio, exposto a emboscadas ou perdendo ouro e experiência.',
    },
    {
      title: isEs ? 'Crash (Colisión)' : 'Crash (Colisão sob a Torre)',
      condition: isEs ? 'Empuje agresivo antes de regresar a base' : 'Avanço agressivo antes de retornar à base',
      result: isEs
        ? 'La torre rival elimina tus súbditos mientras vuelves a base, negando oro al rival y reiniciando la oleada en el centro.'
        : 'A torre rival elimina suas tropas enquanto você volta à base, negando ouro e reiniciando a leva no centro.',
    },
    {
      title: isEs ? 'Cross-Mapping (Respuesta Cruzada)' : 'Cross-Mapping (Resposta Cruzada)',
      condition: isEs ? 'Jungla o 3+ rivales aparecen en el lado opuesto del mapa' : 'Caçador ou 3+ inimigos aparecem no lado oposto do mapa',
      result: isEs
        ? 'No corras tarde hacia la pelea perdida: castiga de inmediato tomando la torre opuesta, robando campamentos enemigos o asegurando el objetivo contrario.'
        : 'Não corra tarde para uma luta perdida: puna imediatamente cobrando a torre oposta ou garantindo o objetivo neutro contrário.',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-[#091428] border border-slate-800 p-4 rounded-xl shadow-md">
        <h1 className="text-xl font-black text-amber-300 tracking-wide flex items-center gap-2">
          <Clock className="w-5 h-5 text-amber-400" />
          <span>Manual Macro Soberano: Tiempos y Control de Oleadas</span>
        </h1>
        <p className="text-xs text-gray-400 mt-0.5">
          Temporizadores verificables de Wild Rift, gestión milimétrica de oleadas, cross-mapping y tempos de base.
        </p>
      </div>

      {/* Timers Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {objectives.map((obj, i) => (
          <div
            key={i}
            className={`bg-[#091428] border ${obj.color} rounded-xl p-4 shadow-lg flex flex-col justify-between space-y-3`}
          >
            <div>
              <div className="flex items-center justify-between border-b border-slate-800 pb-2 mb-2">
                <span className="text-xl font-mono font-black text-white">{obj.time}</span>
                <span className="text-[11px] font-bold text-gray-400">{obj.lane}</span>
              </div>
              <h3 className="text-sm font-bold text-white">{obj.name}</h3>
              <p className="text-xs text-gray-300 mt-1 leading-relaxed">{obj.desc}</p>
            </div>
            <div className="pt-2 border-t border-slate-800/80 flex items-center text-[10px] text-amber-300 font-semibold">
              <Award className="w-3.5 h-3.5 mr-1 text-amber-400" />
              <span>Criterio Soberano de Prioridad</span>
            </div>
          </div>
        ))}
      </div>

      {/* Wave Management Card */}
      <div className="bg-[#091428] border border-amber-500/40 rounded-2xl p-6 shadow-xl space-y-4">
        <div className="flex items-center space-x-2 text-amber-300 border-b border-slate-800 pb-3">
          <Zap className="w-5 h-5 text-amber-400" />
          <h2 className="text-base font-black uppercase tracking-wider">
            Control de Oleadas y Presión de Mapa
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {waveRules.map((rule, idx) => (
            <div
              key={idx}
              className="bg-slate-900/80 border border-slate-800 p-4 rounded-xl space-y-2"
            >
              <h3 className="text-xs font-bold text-amber-400 uppercase tracking-wide">
                {rule.title}
              </h3>
              <div className="text-xs text-gray-400">
                <strong className="text-gray-300">Condición: </strong>
                {rule.condition}
              </div>
              <p className="text-xs text-gray-200 leading-relaxed bg-slate-800/50 p-2.5 rounded border border-slate-700/50">
                {rule.result}
              </p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
