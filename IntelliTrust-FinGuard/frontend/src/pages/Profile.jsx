import { useEffect, useState } from 'react';
import { authApi, demoApi, trustScoreApi } from '../api/endpoints';
import { RotateCcw, ShieldCheck } from 'lucide-react';

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [trust, setTrust] = useState(null);
  const [resetting, setResetting] = useState(false);
  const [message, setMessage] = useState('');

  const load = () => {
    authApi.me().then((res) => setProfile(res.data));
    trustScoreApi.get().then((res) => setTrust(res.data));
  };

  useEffect(load, []);

  const handleReset = async () => {
    setResetting(true);
    setMessage('');
    try {
      await demoApi.reset();
      setMessage('Demo data has been restored to the clean baseline state.');
      load();
    } finally {
      setResetting(false);
    }
  };

  if (!profile) return <div className="page-loading">Loading profile…</div>;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Profile</h1>
          <p className="page-subtitle">Account details and demo controls</p>
        </div>
      </div>

      <div className="panel">
        <h3>Account</h3>
        <div className="profile-grid">
          <div>
            <span className="profile-label">Name</span>
            <div>{profile.name}</div>
          </div>
          <div>
            <span className="profile-label">Email</span>
            <div>{profile.email}</div>
          </div>
          <div>
            <span className="profile-label">Member Since</span>
            <div>{new Date(profile.createdAt).toLocaleDateString()}</div>
          </div>
        </div>
      </div>

      {trust && (
        <div className="panel">
          <h3>
            <ShieldCheck size={18} /> Trust Score
          </h3>
          <div className="trust-score-display">{trust.trustScore.toFixed(1)} / 100</div>
          <p>
            <b>{trust.status}</b> &mdash; {trust.explanation}
          </p>
        </div>
      )}

      <div className="panel">
        <h3>Demo Controls</h3>
        <p>Restore this account to the clean seeded demo state (6 months of realistic transactions, zero alerts, trust score 100).</p>
        <button className="btn-danger" onClick={handleReset} disabled={resetting}>
          <RotateCcw size={16} /> {resetting ? 'Resetting…' : 'Reset Demo Data'}
        </button>
        {message && <div className="info-banner">{message}</div>}
      </div>
    </div>
  );
}
