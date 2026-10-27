import { useEffect, useRef } from 'react';
import { behaviourApi } from '../api/endpoints';

const SAMPLE_INTERVAL_MS = 15000;

/**
 * Passively measures typing rhythm, mouse speed and click rate and reports
 * only the derived metrics every 15 seconds. It never reads or stores which
 * keys were pressed -- only the timestamps between key-down events.
 */
export default function BehaviourTracker() {
  const keyTimestamps = useRef([]);
  const mouseDistance = useRef(0);
  const lastMousePos = useRef(null);
  const clickCount = useRef(0);
  const windowStart = useRef(Date.now());

  useEffect(() => {
    const handleKeyDown = () => {
      keyTimestamps.current.push(Date.now());
    };

    const handleMouseMove = (e) => {
      const prev = lastMousePos.current;
      if (prev) {
        const dx = e.clientX - prev.x;
        const dy = e.clientY - prev.y;
        mouseDistance.current += Math.sqrt(dx * dx + dy * dy);
      }
      lastMousePos.current = { x: e.clientX, y: e.clientY };
    };

    const handleClick = () => {
      clickCount.current += 1;
    };

    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('click', handleClick);

    const interval = setInterval(() => {
      const now = Date.now();
      const elapsedSeconds = (now - windowStart.current) / 1000;
      const keys = keyTimestamps.current;

      let avgKeyIntervalMs = 0;
      if (keys.length > 1) {
        const intervals = [];
        for (let i = 1; i < keys.length; i++) intervals.push(keys[i] - keys[i - 1]);
        avgKeyIntervalMs = intervals.reduce((a, b) => a + b, 0) / intervals.length;
      }
      const words = keys.length / 5;
      const typingSpeedWpm = elapsedSeconds > 0 ? (words / elapsedSeconds) * 60 : 0;
      const mouseSpeedPxS = elapsedSeconds > 0 ? mouseDistance.current / elapsedSeconds : 0;
      const clickRatePerMin = elapsedSeconds > 0 ? (clickCount.current / elapsedSeconds) * 60 : 0;

      if (keys.length > 0 || mouseDistance.current > 0 || clickCount.current > 0) {
        behaviourApi
          .submitEvent({
            typingSpeedWpm: Math.round(typingSpeedWpm * 100) / 100,
            avgKeyIntervalMs: Math.round(avgKeyIntervalMs * 100) / 100,
            mouseSpeedPxS: Math.round(mouseSpeedPxS * 100) / 100,
            clickRatePerMin: Math.round(clickRatePerMin * 100) / 100,
            sessionDurationS: Math.round(elapsedSeconds)
          })
          .catch(() => {});
      }

      keyTimestamps.current = [];
      mouseDistance.current = 0;
      clickCount.current = 0;
      windowStart.current = now;
    }, SAMPLE_INTERVAL_MS);

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('click', handleClick);
      clearInterval(interval);
    };
  }, []);

  return null;
}
