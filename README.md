# Hermes Mobile

Android client untuk mentok prompt ke **Hermes gateway** yang jalan di laptop/server.
Cukup HP + koneksi ke mesin itu — nav bar memakai miuix liquid glass (draggable capsule).

## Siapkan gateway (laptop/server)

Tambahkan ke `~/.hermes/.env`:

```
API_SERVER_ENABLED=true
API_SERVER_HOST=0.0.0.0
API_SERVER_KEY=ganti-dengan-kunci-rahasia
```

Lalu jalankan:

```bash
hermes gateway
```

Default bind API server adalah `127.0.0.1`, jadi `API_SERVER_HOST=0.0.0.0` wajib supaya HP bisa menjangkaunya.
Pastikan HP & mesin satu jaringan, dan firewall mengizinkan port 8642.

## Pasang & pakai

1. Download APK dari artifact GitHub Actions (atau build sendiri: `./gradlew assembleDebug` di CI).
2. Buka tab **Pengaturan**:
   - Alamat server: `http://IP_LAPTOP:8642`
   - API key: nilai `API_SERVER_KEY`
   - Profil: kosongkan (atau isi untuk `/p/<profile>`)
   - Tekan **Uji koneksi** → harus "Terhubung"
3. Tab **Obrolan** → ketik prompt → kirim. Respons streaming + indikator tool.
4. Tab **Riwayat** → pindah/hapus obrolan. `X-Hermes-Session-Id` dipakai supaya sesi tetap nyambung.

## Endpoint yang dipakai

- `GET  /health`
- `POST /v1/chat/completions` (OpenAI-compatible, SSE)

## Build

GitHub Actions: push ke `main` → `app-debug.apk` + `app-release.apk` di artifact.
