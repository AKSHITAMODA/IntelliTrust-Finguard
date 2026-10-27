import { useEffect, useState } from 'react';
import { behaviourApi } from '../api/endpoints';
import { Activity, Keyboard, MousePointer2, MousePointerClick } from 'lucide-react';
import StatCard from '../components/StatCard';

export default function Behaviour() {
  const [status, setStatus] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = () => {
      behaviourApi
        .status()
        .then((res) => setStatus(res.data))
        .catch(() => setError('Unable to load behaviour status.'));
    };
    load();
    const interval = setInterval(load, 15000);
    return () => clearInterval(interval);
  }, []);

  if (error) return <div className="page-error">{error}</div>;
  if (!status) return <div className="page-loading">Loading behaviour status…</div>;

  const latest = status.recentSamples[0];
  const tone = status.status === 'NORMAL' || status.status === 'LEARNING' ? 'good' : 'warn';

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Behaviour Monitoring</h1>
          <p className="page-subtitle">
            Passive biometrics from typing rhythm, mouse movement and clicks &mdash; actual keystrokes are never captured or stored.
          </p>
        </div>
      </div>

      <div className="stat-grid">
        <StatCard icon={Activity} label="Status" value={status.status} tone={tone} />
        <StatCard icon={Keyboard} label="Typing Speed" value={latest ? `${latest.typingSpeedWpm.toFixed(0)} WPM` : '—'} />
        <StatCard icon={MousePointer2} label="Mouse Speed" value={latest ? `${latest.mouseSpeedPxS.toFixed(0)} px/s` : '—'} />
        <StatCard icon={MousePointerClick} label="Click Rate" value={latest ? `${latest.clickRatePerMin.toFixed(0)} /min` : '—'} />
      </div>

      <div className="panel">
        <h3>Recent Samples</h3>
        {!status.baselineEstablished && (
          <p className="info-banner">Establishing your personal baseline &mdash; keep using the app normally.</p>
        )}
        <table className="data-table">
          <thead>
            <tr>
              <th>Captured</th>
              <th>Typing (WPM)</th>
              <th>Key Interval (ms)</th>
              <th>Mouse (px/s)</th>
              <th>Clicks/min</th>
              <th>Risk</th>
              <th>Reason</th>
            </tr>
          </thead>
          <tbody>
            {status.recentSamples.map((s, idx) => (
              <tr key={idx} className={s.anomaly ? 'row-fraud' : ''}>
                <td>{new Date(s.capturedAt).toLocaleString()}</td>
                <td>{s.typingSpeedWpm.toFixed(0)}</td>
                <td>{s.avgKeyIntervalMs.toFixed(0)}</td>
                <td>{s.mouseSpeedPxS.toFixed(0)}</td>
                <td>{s.clickRatePerMin.toFixed(0)}</td>
                <td>
                  <span className={`risk-pill ${s.anomaly ? 'risk-high' : 'risk-low'}`}>{s.riskScore.toFixed(0)}</span>
                </td>
                <td>{s.reason}</td>
              </tr>
            ))}
            {status.recentSamples.length === 0 && (
              <tr>
                <td colSpan={7}>No behaviour samples captured yet. Use the app for 15+ seconds.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
