import { useRef, useState } from 'react';
import type { ScannedDataSize } from '../api/types.ts';
import { IconPlus, Modal } from './ui.tsx';

export function ImportModal({
  open,
  onClose,
  onUpload,
  busy,
  counts,
  hasScan,
}: {
  open: boolean;
  onClose: () => void;
  onUpload: (file: File) => Promise<boolean>;
  busy: boolean;
  counts: ScannedDataSize | null;
  hasScan: boolean;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [dragging, setDragging] = useState(false);

  if (!open) return null;

  async function pick(file: File | undefined) {
    if (!file) return;
    const imported = await onUpload(file);
    if (imported) onClose();
  }

  return (
    <Modal
      title="Import scanner data"
      subtitle="Drop a HSR scanner JSON export to replace the current inventory."
      onClose={onClose}
      width="regular"
    >
      <div
        className={`dropzone ${dragging ? 'dragging' : ''}`}
        onDragOver={(event) => {
          event.preventDefault();
          setDragging(true);
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={(event) => {
          event.preventDefault();
          setDragging(false);
          void pick(event.dataTransfer.files?.[0]);
        }}
        onClick={() => inputRef.current?.click()}
        role="button"
        tabIndex={0}
        onKeyDown={(event) => {
          if (event.key === 'Enter' || event.key === ' ') inputRef.current?.click();
        }}
      >
        <input
          ref={inputRef}
          type="file"
          accept="application/json,.json"
          hidden
          onChange={(event) => {
            void pick(event.target.files?.[0]);
            event.target.value = '';
          }}
        />
        <IconPlus />
        <strong>{dragging ? 'Drop the JSON file' : 'Choose or drop a scan JSON'}</strong>
        <span className="muted">The import replaces characters, light cones and relics for this session.</span>
      </div>

      {counts ? (
        <div className="import-counts">
          <div>
            <span className="muted">Characters</span>
            <strong>{counts.numCharacters}</strong>
          </div>
          <div>
            <span className="muted">Light cones</span>
            <strong>{counts.numLightCones}</strong>
          </div>
          <div>
            <span className="muted">Relics</span>
            <strong>{counts.numRelics}</strong>
          </div>
        </div>
      ) : null}

      {!hasScan ? (
        <p className="muted">
          You can also add relics, light cones and characters manually after the first import.
        </p>
      ) : null}

      <div className="modal-actions">
        <button type="button" className="btn" onClick={onClose} disabled={busy}>
          {busy ? 'Importing…' : 'Close'}
        </button>
      </div>
    </Modal>
  );
}
