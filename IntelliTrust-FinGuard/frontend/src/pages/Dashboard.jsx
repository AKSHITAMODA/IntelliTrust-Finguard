import { useEffect, useState } from 'react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  Legend
} from 'recharts';
import { Wallet, TrendingUp, Sparkles, ShieldCheck, BellRing, Activity } from 'lucide-react';
import { dashboardApi, transactionsApi } from '../api/endpoints';
import StatCard from '../components/StatCard';

const COLORS = ['#6366f1', '#22d3ee', '#f59e0b', '#ef4444', '#10b981', '#a855f7'];
const currency = (v) => `₹${Number(v).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [simLoading, setSimLoading] = useState(false);

  const load = () => {
    dashboardApi
      .get()
      .then((res) => setData(res.data))
      .catch(() => setError('Unable to load dashboard data.'));
  };

  useEffect(load, []);

  const handleSimulate = async (forceSuspicious) => {
    setSimLoading(true);
    try {
      await transactionsApi.simulate({ count: 1, forceSuspicious });
      load();
    } finally {
      setSimLoading(false);
    }
  };

  if (error) return <div className="page-error">{error}</div>;
  if (!data) return <div className="page-loading">Loading dashboard…</div>;

  const behaviourTone = data.behaviourStatus === 'NORMAL' || data.behaviourStatus === 'LEARNING' ? 'good' : 'warn';

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p className="page-subtitle">Real-time financial security overview</p>
        </div>
        <div className="page-actions">
          <button className="btn-secondary" disabled={simLoading} onClick={() => handleSimulate(false)}>
            Simulate Normal Txn
          </button>
          <button className="btn-danger" disabled={simLoading} onClick={() => handleSimulate(true)}>
            Simulate Suspicious Txn
          </button>
        </div>
      </div>

      <div className="stat-grid">
        <StatCard icon={Wallet} label="Net Balance" value={currency(data.netBalance)} tone="good" />
        <StatCard icon={TrendingUp} label="Monthly Spending" value={currency(data.monthlySpending)} />
        <StatCard
          icon={Sparkles}
          label="AI Forecast"
          value={currency(data.forecastAmount)}
          sub={data.forecastMonthLabel}
        />
        <StatCard icon={ShieldCheck} label="Trust Score" value={`${data.trustScore.toFixed(1)} / 100`} tone={data.trustScore >= 70 ? 'good' : 'warn'} />
        <StatCard icon={BellRing} label="Active Alerts" value={data.activeAlerts} tone={data.activeAlerts > 0 ? 'warn' : 'good'} />
        <StatCard icon={Activity} label="Behaviour Status" value={data.behaviourStatus} tone={behaviourTone} />
      </div>

      <div className="chart-grid">
        <div className="panel">
          <h3>Historical Spending</h3>
          <ResponsiveContainer width="100%" height={280}>
            <LineChart data={data.historicalSpending}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border-color)" />
              <XAxis dataKey="month" stroke="var(--text-muted)" />
              <YAxis stroke="var(--text-muted)" />
              <Tooltip
                formatter={(value) => currency(value)}
                contentStyle={{ background: 'var(--panel-bg)', border: '1px solid var(--border-color)' }}
              />
              <Line type="monotone" dataKey="total" stroke="#6366f1" strokeWidth={3} dot={{ r: 4 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="panel">
          <h3>Category Spending (This Period)</h3>
          <ResponsiveContainer width="100%" height={280}>
            <PieChart>
              <Pie
                data={data.categoryBreakdown}
                dataKey="total"
                nameKey="category"
                cx="50%"
                cy="50%"
                outerRadius={95}
                label={({ category }) => category}
              >
                {data.categoryBreakdown.map((entry, index) => (
                  <Cell key={entry.category} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip formatter={(value) => currency(value)} />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="panel">
        <h3>Recent Transactions</h3>
        <table className="data-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Merchant</th>
              <th>Category</th>
              <th>Type</th>
              <th>Amount</th>
              <th>Risk</th>
            </tr>
          </thead>
          <tbody>
            {data.recentTransactions.map((t) => (
              <tr key={t.id} className={t.fraud ? 'row-fraud' : ''}>
                <td>{new Date(t.txnDate).toLocaleString()}</td>
                <td>{t.merchant}</td>
                <td>{t.category}</td>
                <td>{t.txnType}</td>
                <td>{currency(t.amount)}</td>
                <td>
                  <span className={`risk-pill ${t.fraud ? 'risk-high' : 'risk-low'}`}>{t.riskScore.toFixed(0)}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
