import { useEffect, useRef } from 'react';
import { locationApi } from '../api/endpoints';

const MIN_SEND_INTERVAL_MS = 20000;

/** Reports the browser's live geolocation to the backend for geo-risk scoring. */
export default function LocationTracker() {
  const lastSent = useRef(0);

  useEffect(() => {
    if (!('geolocation' in navigator)) return undefined;

    const watchId = navigator.geolocation.watchPosition(
      (position) => {
        const now = Date.now();
        if (now - lastSent.current < MIN_SEND_INTERVAL_MS) return;
        lastSent.current = now;
        locationApi
          .submit({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            accuracy: position.coords.accuracy
          })
          .catch(() => {});
      },
      () => {},
      { enableHighAccuracy: false, maximumAge: 15000, timeout: 10000 }
    );

    return () => navigator.geolocation.clearWatch(watchId);
  }, []);

  return null;
}
