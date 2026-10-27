import { useEffect, useRef, useState } from 'react';
import { transactionsApi, fraudApi } from '../api/endpoints';
import { UploadCloud, Trash2, Sparkles } from 'lucide-react';

const CATEGORIES = ['FOOD', 'SHOPPING', 'TRAVEL', 'BILLS', 'INCOME', 'OTHER'];
const currency = (v) => `₹${Number(v).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

const emptyForm = {
  txnDate: new Date().toISOString().slice(0, 16),
  description: '',
  merchant: '',
  category: 'OTHER',
  amount: '',
  txnType: 'DEBIT'
};

export default function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [filters, setFilters] = useState({ category: '', type: '', fraudOnly: false, search: '' });
  const [form, setForm] = useState(emptyForm);
  const [explanation, setExplanation] = useState(null);
  const [importResult, setImportResult] = useState(null);
  const [error, setError] = useState('');
  const fileInputRef = useRef(null);

  const load = () => {
    const params = {};
    if (filters.category) params.category = filters.category;
    if (filters.type) params.type = filters.type;
    if (filters.fraudOnly) params.fraudOnly = true;
    if (filters.search) params.search = filters.search;
    transactionsApi
      .list(params)
      .then((res) => setTransactions(res.data))
      .catch(() => setError('Unable to load transactions.'));
  };

  useEffect(load, [filters.category, filters.type, filters.fraudOnly]);

  const handleSearch = (e) => {
    e.preventDefault();
    load();
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await transactionsApi.create({
        ...form,
        amount: Number(form.amount),
        txnDate: form.txnDate
      });
      setForm(emptyForm);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to create transaction.');
    }
  };

  const handleDelete = async (id) => {
    await transactionsApi.remove(id);
    load();
  };

  const handleExplain = async (id) => {
    const res = await fraudApi.explain(id);
    setExplanation(res.data);
  };

  const handleImport = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setError('');
    try {
      const res = await transactionsApi.importCsv(file);
      setImportResult(res.data);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'CSV import failed.');
    } finally {
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Transactions</h1>
          <p className="page-subtitle">CRUD, filtering, CSV import &amp; explainable fraud scoring</p>
        </div>
        <div className="page-actions">
          <input ref={fileInputRef} type="file" accept=".csv" onChange={handleImport} style={{ display: 'none' }} />
          <button className="btn-secondary" onClick={() => fileInputRef.current.click()}>
            <UploadCloud size={16} /> Import CSV
          </button>
        </div>
      </div>

      {error && <div className="form-error">{error}</div>}
      {importResult && (
        <div className="panel info-banner">
          Imported {importResult.imported} of {importResult.totalRows} rows &middot; {importResult.duplicatesSkipped} duplicates skipped &middot;{' '}
          {importResult.invalidSkipped} invalid rows.
        </div>
      )}

      <div className="panel">
        <h3>Add Transaction</h3>
        <form className="inline-form" onSubmit={handleCreate}>
          <input
            type="datetime-local"
            value={form.txnDate}
            onChange={(e) => setForm({ ...form, txnDate: e.target.value })}
            required
          />
          <input
            type="text"
            placeholder="Merchant"
            value={form.merchant}
            onChange={(e) => setForm({ ...form, merchant: e.target.value })}
          />
          <input
            type="text"
            placeholder="Description"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
          <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
          <select value={form.txnType} onChange={(e) => setForm({ ...form, txnType: e.target.value })}>
            <option value="DEBIT">DEBIT</option>
            <option value="CREDIT">CREDIT</option>
          </select>
          <input
            type="number"
            step="0.01"
            placeholder="Amount"
            value={form.amount}
            onChange={(e) => setForm({ ...form, amount: e.target.value })}
            required
          />
          <button type="submit" className="btn-primary">
            Add
          </button>
        </form>
      </div>

      <div className="panel">
        <form className="inline-form" onSubmit={handleSearch}>
          <select value={filters.category} onChange={(e) => setFilters({ ...filters, category: e.target.value })}>
            <option value="">All categories</option>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
          <select value={filters.type} onChange={(e) => setFilters({ ...filters, type: e.target.value })}>
            <option value="">All types</option>
            <option value="DEBIT">DEBIT</option>
            <option value="CREDIT">CREDIT</option>
          </select>
          <label className="checkbox-label">
            <input
              type="checkbox"
              checked={filters.fraudOnly}
              onChange={(e) => setFilters({ ...filters, fraudOnly: e.target.checked })}
            />
            Fraud only
          </label>
          <input
            type="text"
            placeholder="Search description or merchant"
            value={filters.search}
            onChange={(e) => setFilters({ ...filters, search: e.target.value })}
          />
          <button type="submit" className="btn-secondary">
            Search
          </button>
        </form>

        <table className="data-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Merchant</th>
              <th>Category</th>
              <th>Type</th>
              <th>Source</th>
              <th>Amount</th>
              <th>Risk</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((t) => (
              <tr key={t.id} className={t.fraud ? 'row-fraud' : ''}>
                <td>{new Date(t.txnDate).toLocaleString()}</td>
                <td>{t.merchant}</td>
                <td>{t.category}</td>
                <td>{t.txnType}</td>
                <td>{t.source}</td>
                <td>{currency(t.amount)}</td>
                <td>
                  <button className="link-btn" onClick={() => handleExplain(t.id)}>
                    <span className={`risk-pill ${t.fraud ? 'risk-high' : 'risk-low'}`}>{t.riskScore.toFixed(0)}</span>
                  </button>
                </td>
                <td>
                  <button className="icon-btn" onClick={() => handleDelete(t.id)} title="Delete">
                    <Trash2 size={16} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {explanation && (
        <div className="modal-backdrop" onClick={() => setExplanation(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>
              <Sparkles size={18} /> Fraud Explanation
            </h3>
            <div className="explain-grid">
              <div>
                Amount Risk: <b>{explanation.amountRisk.toFixed(0)}</b>
              </div>
              <div>
                Frequency Risk: <b>{explanation.frequencyRisk.toFixed(0)}</b>
              </div>
              <div>
                Anomaly Score: <b>{explanation.anomalyScore.toFixed(0)}</b>
              </div>
              <div>
                Geo Risk: <b>{explanation.geoRisk.toFixed(0)}</b>
              </div>
              <div>
                Behavioural Risk: <b>{explanation.behaviouralRisk.toFixed(0)}</b>
              </div>
              <div>
                Combined Risk: <b>{explanation.riskScore.toFixed(0)} / 100</b>
              </div>
            </div>
            <ul className="reason-list">
              {explanation.reasons.map((r) => (
                <li key={r}>{r}</li>
              ))}
            </ul>
            <button className="btn-secondary" onClick={() => setExplanation(null)}>
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
