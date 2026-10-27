import { useEffect, useState } from 'react';
import { alertsApi } from '../api/endpoints';
import { CheckCircle2, ShieldAlert } from 'lucide-react';

export default function Alerts() {
  const [alerts, setAlerts] = useState([]);
  const [error, setError] = useState('');

  const load = () => {
    alertsApi
      .list()
      .then((res) => setAlerts(res.data))
      .catch(() => setError('Unable to load alerts.'));
  };

  useEffect(load, []);

  const handleResolve = async (id) => {
    await alertsApi.resolve(id);
    load();
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Alerts</h1>
          <p className="page-subtitle">Fraud and behavioural anomaly alerts</p>
        </div>
      </div>

      {error && <div className="form-error">{error}</div>}

      <div className="alert-list">
        {alerts.length === 0 && <div className="panel">No alerts &mdash; everything looks normal.</div>}
        {alerts.map((a) => (
          <div key={a.id} className={`alert-card severity-${a.severity.toLowerCase()} ${a.resolved ? 'resolved' : ''}`}>
            <ShieldAlert size={20} />
            <div className="alert-body">
              <div className="alert-meta">
                <span className="alert-type">{a.alertType}</span>
                <span className="alert-severity">{a.severity}</span>
                <span className="alert-date">{new Date(a.createdAt).toLocaleString()}</span>
              </div>
              <div className="alert-message">{a.message}</div>
            </div>
            {!a.resolved && (
              <button className="btn-secondary" onClick={() => handleResolve(a.id)}>
                <CheckCircle2 size={16} /> Resolve
              </button>
            )}
            {a.resolved && <span className="resolved-badge">Resolved</span>}
          </div>
        ))}
      </div>
    </div>
  );
}
