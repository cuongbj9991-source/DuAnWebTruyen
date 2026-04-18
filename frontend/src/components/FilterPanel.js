import React, { useState, useEffect } from 'react';
import '../styles/FilterPanel.css';

function FilterPanel({ onFilterChange, initialFilters = {} }) {
  const [filters, setFilters] = useState(initialFilters);
  const [filterOptions, setFilterOptions] = useState(null);
  const [expandedSections, setExpandedSections] = useState({
    search: true,
    chapters: false,
    type: false,
    genres: false,
    status: false
  });

  useEffect(() => {
    fetchFilterOptions();
  }, []);

  const fetchFilterOptions = async () => {
    try {
      const baseURL = process.env.REACT_APP_STORY_SERVICE || 'http://localhost:8081/api';
      const response = await fetch(`${baseURL}/stories/filter-options`);
      const data = await response.json();
      setFilterOptions(data);
    } catch (error) {
      console.error('Error fetching filter options:', error);
    }
  };

  const toggleSection = (section) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  const handleSearchChange = (e) => {
    const newFilters = { ...filters, search: e.target.value };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleSummarySearchChange = (e) => {
    const newFilters = { ...filters, search_summary: e.target.value };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleChaptersChange = (value) => {
    const newFilters = { ...filters };
    if (value === 'all') {
      delete newFilters.min_chapters;
    } else {
      newFilters.min_chapters = value;
    }
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleGenreChange = (genre) => {
    let genres = filters.genre ? filters.genre.split(',') : [];
    if (genres.includes(genre)) {
      genres = genres.filter(g => g !== genre);
    } else {
      genres.push(genre);
    }
    const newFilters = {
      ...filters,
      genre: genres.length > 0 ? genres.join(',') : undefined
    };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleStoryTypeChange = (type) => {
    let types = filters.story_type ? filters.story_type.split(',') : [];
    if (types.includes(type)) {
      types = types.filter(t => t !== type);
    } else {
      types.push(type);
    }
    const newFilters = {
      ...filters,
      story_type: types.length > 0 ? types.join(',') : undefined
    };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleStatusChange = (status) => {
    let statuses = filters.status ? filters.status.split(',') : [];
    if (statuses.includes(status)) {
      statuses = statuses.filter(s => s !== status);
    } else {
      statuses.push(status);
    }
    const newFilters = {
      ...filters,
      status: statuses.length > 0 ? statuses.join(',') : undefined
    };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleSortChange = (e) => {
    const newFilters = { ...filters, sortBy: e.target.value };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const getSelectedGenres = () => {
    return filters.genre ? filters.genre.split(',') : [];
  };

  const getSelectedTypes = () => {
    return filters.story_type ? filters.story_type.split(',') : [];
  };

  const getSelectedStatuses = () => {
    return filters.status ? filters.status.split(',') : [];
  };

  if (!filterOptions) return <div>Loading filters...</div>;

  return (
    <div className="filter-panel">
      <div className="filter-section">
        <h3>🔍 Tìm Kiếm</h3>
        <div className="search-group">
          <label>Tìm từ khóa:</label>
          <input
            type="text"
            placeholder="Tên, tên hán việt, tác giả"
            value={filters.search || ''}
            onChange={handleSearchChange}
            className="filter-input"
          />
        </div>
        <div className="search-group">
          <label>Tìm tóm tắt:</label>
          <input
            type="text"
            placeholder="Tìm trong phần tóm tắt"
            value={filters.search_summary || ''}
            onChange={handleSummarySearchChange}
            className="filter-input"
          />
        </div>
      </div>

      <div className="filter-section">
        <h3 
          onClick={() => toggleSection('chapters')}
          className="filter-title clickable"
        >
          📖 Số Chương {expandedSections.chapters ? '▼' : '▶'}
        </h3>
        {expandedSections.chapters && (
          <div className="filter-buttons">
            <button
              className={!filters.min_chapters ? 'active' : ''}
              onClick={() => handleChaptersChange('all')}
            >
              Tất cả
            </button>
            {[50, 100, 200, 500, 1000, 1500, 2000].map(num => (
              <button
                key={num}
                className={filters.min_chapters === num ? 'active' : ''}
                onClick={() => handleChaptersChange(num)}
              >
                &gt; {num}
              </button>
            ))}
          </div>
        )}
      </div>

      <div className="filter-section">
        <h3 
          onClick={() => toggleSection('type')}
          className="filter-title clickable"
        >
          📝 Loại Truyện {expandedSections.type ? '▼' : '▶'}
        </h3>
        {expandedSections.type && (
          <div className="filter-buttons">
            {filterOptions?.story_types?.length > 0 ? filterOptions.story_types.map(type => (
              <button
                key={type}
                className={getSelectedTypes().includes(type) ? 'active' : ''}
                onClick={() => handleStoryTypeChange(type)}
              >
                {type}
              </button>
            )) : (
              <p>Không có dữ liệu</p>
            )}
          </div>
        )}
      </div>

      <div className="filter-section">
        <h3 
          onClick={() => toggleSection('genres')}
          className="filter-title clickable"
        >
          🎭 Thể Loại {expandedSections.genres ? '▼' : '▶'}
        </h3>
        {expandedSections.genres && (
          <div className="filter-buttons">
            {filterOptions?.genres?.length > 0 ? filterOptions.genres.map(genre => (
              <button
                key={genre}
                className={getSelectedGenres().includes(genre) ? 'active' : ''}
                onClick={() => handleGenreChange(genre)}
              >
                {genre}
              </button>
            )) : (
              <p>Không có dữ liệu</p>
            )}
          </div>
        )}
      </div>

      <div className="filter-section">
        <h3 
          onClick={() => toggleSection('status')}
          className="filter-title clickable"
        >
          📊 Trạng Thái {expandedSections.status ? '▼' : '▶'}
        </h3>
        {expandedSections.status && (
          <div className="filter-buttons">
            <button
              className={getSelectedStatuses().includes('ongoing') ? 'active' : ''}
              onClick={() => handleStatusChange('ongoing')}
            >
              Còn tiếp
            </button>
            <button
              className={getSelectedStatuses().includes('completed') ? 'active' : ''}
              onClick={() => handleStatusChange('completed')}
            >
              Hoàn thành
            </button>
            <button
              className={getSelectedStatuses().includes('paused') ? 'active' : ''}
              onClick={() => handleStatusChange('paused')}
            >
              Tạm ngưng
            </button>
          </div>
        )}
      </div>

      <div className="filter-section">
        <h3>⬆️ Sắp Xếp</h3>
        <select 
          value={filters.sortBy || 'views_total'}
          onChange={handleSortChange}
          className="filter-select"
        >
          {filterOptions?.sort_options?.length > 0 ? filterOptions.sort_options.map(option => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          )) : (
            <option>Mặc định</option>
          )}
        </select>
      </div>

      {Object.values(filters).some(v => v) && (
        <button 
          className="reset-button"
          onClick={() => {
            setFilters({});
            onFilterChange({});
          }}
        >
          🔄 Xóa Bộ Lọc
        </button>
      )}
    </div>
  );
}

export default FilterPanel;
