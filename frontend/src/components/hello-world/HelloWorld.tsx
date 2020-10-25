import React from 'react';
import styles from './HelloWorld.module.css';

export interface HelloWorldProps {
  what: string;
}

export const HelloWorld: React.FC<HelloWorldProps> = ({ what }) => {
  return (
    <header className={styles.header}>
      <p>Hello {what}!</p>
    </header>
  );
};
