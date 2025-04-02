import React from 'react'


/* style */
import styles from './SearchBar.module.css'

const SearchBar = () => {
  return (
    <div className={styles.searchbar}>
        <input
        className={styles.searchbar_input}
        type="search"
        id='search-bar'
        placeholder="Search..." />
        
        <button className={styles.searchbar_btn}>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className={styles.searchbar_btn_icon}>
                <path fillRule="evenodd" d="M10.5 3.75a6.75 6.75 0 1 0 0 13.5 6.75 6.75 0 0 0 0-13.5ZM2.25 10.5a8.25 8.25 0 1 1 14.59 5.28l4.69 4.69a.75.75 0 1 1-1.06 1.06l-4.69-4.69A8.25 8.25 0 0 1 2.25 10.5Z" clipRule="evenodd" />
            </svg>
        </button>
    </div>
  )
}

export default SearchBar