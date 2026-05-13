import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  createEquipment,
  updateEquipment,
  getEquipmentById,
  getAllCategories,
} from '../api/apiService.js';

const EMPTY_FORM = {
  name: '',
  description: '',
  categoryId: '',
  totalQuantity: '',
  availableQuantity: '',
  status: 'IN_STOCK',
};

export default function EquipmentForm() {
  const { id } = useParams();
  const isEditing = Boolean(id);
  const navigate = useNavigate();

  const [formData, setFormData] = useState(EMPTY_FORM);
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const loadData = async () => {
      try {
        const cats = await getAllCategories();
        setCategories(cats);

        if (isEditing) {
          const equipment = await getEquipmentById(id);
          setFormData({
            name: equipment.name,
            description: equipment.description ?? '',
            categoryId: equipment.category?.id ?? '',
            totalQuantity: equipment.totalQuantity,
            availableQuantity: equipment.availableQuantity,
            status: equipment.status,
          });
        }
      } catch (err) {
        setError('Failed to load data.');
      }
    };
    loadData();
  }, [id, isEditing]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');

    const payload = {
      name: formData.name,
      description: formData.description,
      category: formData.categoryId ? { id: Number(formData.categoryId) } : null,
      totalQuantity: Number(formData.totalQuantity),
      availableQuantity: Number(formData.availableQuantity),
      status: formData.status,
    };

    try {
      if (isEditing) {
        await updateEquipment(id, payload);
      } else {
        await createEquipment(payload);
      }
      navigate('/equipment');
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to save equipment.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="form-container">
      <h1 className="page-title">{isEditing ? 'Edit Equipment' : 'Add Equipment'}</h1>

      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleSubmit} className="form">
        <div className="form-group">
          <label htmlFor="name">Equipment Name *</label>
          <input
            id="name"
            name="name"
            type="text"
            value={formData.name}
            onChange={handleChange}
            required
            placeholder="e.g. JBL SRX835P Speaker"
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows={3}
            placeholder="Brief description of the equipment"
          />
        </div>

        <div className="form-group">
          <label htmlFor="categoryId">Category *</label>
          <select
            id="categoryId"
            name="categoryId"
            value={formData.categoryId}
            onChange={handleChange}
            required
          >
            <option value="">— Select a category —</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>{cat.name}</option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="totalQuantity">Total Quantity *</label>
            <input
              id="totalQuantity"
              name="totalQuantity"
              type="number"
              min="0"
              value={formData.totalQuantity}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="availableQuantity">Available Quantity *</label>
            <input
              id="availableQuantity"
              name="availableQuantity"
              type="number"
              min="0"
              value={formData.availableQuantity}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        {isEditing && (
          <div className="form-group">
            <label htmlFor="status">Status</label>
            <select
              id="status"
              name="status"
              value={formData.status}
              onChange={handleChange}
            >
              <option value="IN_STOCK">In Stock</option>
              <option value="RESERVED">Reserved</option>
              <option value="DEPLOYED">Deployed</option>
              <option value="RETURNED">Returned</option>
            </select>
          </div>
        )}

        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving...' : isEditing ? 'Update Equipment' : 'Add Equipment'}
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/equipment')}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
