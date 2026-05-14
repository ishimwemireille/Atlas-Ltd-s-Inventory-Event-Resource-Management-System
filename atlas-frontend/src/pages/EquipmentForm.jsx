import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  createEquipment,
  updateEquipment,
  getEquipmentById,
  getAllCategories,
} from '../api/apiService.js';

// empty form shape — used both for initialisation and after a successful create
const EMPTY_FORM = {
  name: '',
  description: '',
  categoryId: '',
  totalQuantity: '',
  availableQuantity: '',
  sellingPricePerUnit: '',
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

  // fetch data on component mount — load categories and, if editing, the existing equipment
  useEffect(() => {
    const loadData = async () => {
      try {
        const cats = await getAllCategories();
        setCategories(cats);

        if (isEditing) {
          // populate form with existing values when editing
          const equipment = await getEquipmentById(id);
          setFormData({
            name: equipment.name,
            description: equipment.description ?? '',
            categoryId: equipment.category?.id ?? '',
            totalQuantity: equipment.totalQuantity,
            availableQuantity: equipment.availableQuantity,
            sellingPricePerUnit: equipment.sellingPricePerUnit ?? '',
            status: equipment.status,
          });
        }
      } catch (err) {
        // show error state if API call fails
        setError('Failed to load data.');
      }
    };
    loadData();
  }, [id, isEditing]);

  // single handler for all text/select/number inputs — updates only the changed field
  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    // validate input before making the API call — check required fields are not empty
    if (!formData.name.trim()) {
      setError('Equipment name is required.');
      return;
    }
    if (!formData.categoryId) {
      setError('Please select a category.');
      return;
    }
    if (formData.totalQuantity === '' || formData.availableQuantity === '') {
      setError('Total and available quantities are required.');
      return;
    }

    setSaving(true);
    setError('');

    // build the API payload — convert string inputs to proper types
    const payload = {
      name: formData.name,
      description: formData.description,
      category: formData.categoryId ? { id: Number(formData.categoryId) } : null,
      totalQuantity: Number(formData.totalQuantity),
      availableQuantity: Number(formData.availableQuantity),
      sellingPricePerUnit: formData.sellingPricePerUnit ? Number(formData.sellingPricePerUnit) : null,
      status: formData.status,
    };

    try {
      if (isEditing) {
        await updateEquipment(id, payload);
      } else {
        await createEquipment(payload);
      }
      // navigate back to the list after successful save
      navigate('/equipment');
    } catch (err) {
      // show error state if API call fails — e.g. validation error from the backend
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

        <div className="form-group">
          <label htmlFor="sellingPricePerUnit">Selling Price per Unit (RWF)</label>
          <input id="sellingPricePerUnit" name="sellingPricePerUnit" type="number" min="0" step="100"
            value={formData.sellingPricePerUnit} onChange={handleChange}
            placeholder="e.g. 500000" />
        </div>

        {/* status selector only shown when editing — new equipment always starts as IN_STOCK */}
        {isEditing && (
          <div className="form-group">
            <label htmlFor="status">Status</label>
            <select id="status" name="status" value={formData.status} onChange={handleChange}>
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
