import express from 'express';

const app = express();
app.use(express.json({ verify: (req, _res, buf) => { req.rawBody = buf; } }));

const VERIFY_TOKEN = process.env.WHATSAPP_VERIFY_TOKEN || '';
const ACCESS_TOKEN = process.env.WHATSAPP_ACCESS_TOKEN || '';
const PHONE_NUMBER_ID = process.env.WHATSAPP_PHONE_NUMBER_ID || '';
const GRAPH_VERSION = process.env.META_GRAPH_VERSION || 'v23.0';

const templates = {
  greeting: 'Terima kasih telah menghubungi PT. TANS GLOBAL PERSADA. Kami bergerak di bidang Kontraktor, Supplier, dan General Trading. Silakan sampaikan kebutuhan proyek, lokasi, volume pekerjaan/material, dan target waktu.',
  contractor: 'Untuk kebutuhan KONTRAKTOR/PROYEK, mohon kirim nama pekerjaan, lokasi proyek, ruang lingkup/BOQ bila ada, nilai/pagu perkiraan, target mulai-selesai, dan nama PIC.',
  supplier: 'Untuk kebutuhan SUPPLIER/MATERIAL, mohon kirim nama barang/material, spesifikasi/merk, jumlah, lokasi pengiriman, dan tanggal kebutuhan.',
  survey: 'Untuk SURVEY LOKASI, mohon kirim alamat lengkap atau pin Google Maps, nama PIC di lokasi, jenis pekerjaan, serta pilihan hari dan jam survey.',
  quotation: 'Untuk PENAWARAN/RAB/HARGA, mohon kirim BOQ/RAB/gambar kerja atau daftar item lengkap beserta volume, spesifikasi, lokasi proyek, dan target pelaksanaan.',
  fallback: 'Pesan Anda sudah kami terima. Mohon tuliskan kebutuhan Anda, misalnya: PROYEK KONTRAKTOR, SUPPLIER MATERIAL, SURVEY LOKASI, atau PENAWARAN/RAB.'
};

function chooseReply(text='') {
  const s = text.toLowerCase();
  const has = (...keys) => keys.some(k => s.includes(k));
  if (has('kontraktor','proyek','pekerjaan','spk','tender')) return templates.contractor;
  if (has('supplier','material','barang','semen','besi','baja','pasir','beton')) return templates.supplier;
  if (has('survey','survei','lokasi','site visit')) return templates.survey;
  if (has('penawaran','rab','harga','quotation','boq','estimasi')) return templates.quotation;
  if (has('halo','hallo','hai','assalamualaikum','selamat pagi','selamat siang','selamat sore','selamat malam')) return templates.greeting;
  return templates.fallback;
}

app.get('/health', (_req,res) => res.json({ok:true, service:'TANS WhatsApp Cloud API', version:'3.0.0'}));

app.get('/webhook', (req,res) => {
  const mode = req.query['hub.mode'];
  const token = req.query['hub.verify_token'];
  const challenge = req.query['hub.challenge'];
  if (mode === 'subscribe' && token === VERIFY_TOKEN) return res.status(200).send(challenge);
  return res.sendStatus(403);
});

app.post('/webhook', async (req,res) => {
  res.sendStatus(200);
  try {
    const entry = req.body?.entry?.[0];
    const change = entry?.changes?.[0]?.value;
    const message = change?.messages?.[0];
    if (!message || message.type !== 'text') return;
    const from = message.from;
    const text = message.text?.body || '';
    const reply = chooseReply(text);
    await sendText(from, reply);
  } catch (err) {
    console.error('Webhook error:', err);
  }
});

async function sendText(to, body) {
  if (!ACCESS_TOKEN || !PHONE_NUMBER_ID) throw new Error('WHATSAPP_ACCESS_TOKEN / WHATSAPP_PHONE_NUMBER_ID belum diset');
  const url = `https://graph.facebook.com/${GRAPH_VERSION}/${PHONE_NUMBER_ID}/messages`;
  const r = await fetch(url, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${ACCESS_TOKEN}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      messaging_product: 'whatsapp',
      to,
      type: 'text',
      text: { body }
    })
  });
  if (!r.ok) throw new Error(`Meta API ${r.status}: ${await r.text()}`);
}

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`TANS Cloud API listening on ${port}`));
