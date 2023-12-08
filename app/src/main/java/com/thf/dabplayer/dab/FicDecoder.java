package com.thf.dabplayer.dab;

import android.gesture.Prediction;
import com.thf.dabplayer.utils.Logger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FicDecoder {

  private FIC_ENSEMBLE ensemble = new FIC_ENSEMBLE();
  private Map<Integer, FIC_SERVICE> services = new HashMap<>();
  private Map<Integer, FIC_SUBCHANNEL> subchannels = new HashMap<>();

  public void process(byte[] data, int len) {
    if (len % 32 != 0) {
      // System.err.printf("FICDecoder: Ignoring non-integer FIB count FIC data with %d bytes\n",
      // len);
      Logger.d(
          String.format(
              "FICDecoder: Ignoring non-integer FIB count FIC data with %d bytes\n", len));
      return;
    }
    for (int i = 0; i < len; i += 32) {
      ProcessFIB(Arrays.copyOfRange(data, i, i + 32));
    }
  }

  private void ProcessFIB(byte[] data) {
    int crc_stored = ((data[30] << 8) | data[31]);
    // short crc_calced = CalcCRC.CalcCRC_CRC16_CCITT.Calc(data, 30);
    int crc_calced = calculate_crc(data, 30);
    if (crc_stored != crc_calced) {
      Logger.d(
          "FICDecoder: CRCs are different. Stored: '"
              + crc_stored
              + "' Calculated: '"
              + crc_calced
              + "'");
      return;
    }

    for (int offset = 0; offset < 30 && data[offset] != (byte) 0xFF; ) {
      int type = data[offset] >> 5;
      int len = data[offset] & 0x1F;
      offset++;
      switch (type) {
        case 0:
          Logger.d("FICDecoder: FIG0");
          ProcessFIG0(Arrays.copyOfRange(data, offset, offset + len), len);
          break;
        case 1:
          Logger.d("FICDecoder: FIG1");
          ProcessFIG1(Arrays.copyOfRange(data, offset, offset + len), len);
          break;
      }
      offset += len;
    }
  }

  public static int calculate_crc(byte[] data, int len) {
    int dataIndex = 0;
    short c;
    short[] crc = {0};

    crc[0] = (short) 0xffff;
    for (int j = 1; j <= len; j++, dataIndex++) {
      c = (short) ((Short.toUnsignedInt(crc[0]) >> 8 ^ Byte.toUnsignedInt(data[dataIndex])) << 8);
      for (int i = 0; i < 8; i++) {
        if ((Short.toUnsignedInt(c) & 0x8000) != 0) {
          c = (short) (Short.toUnsignedInt(c) << 1 ^ 0x1021);
        } else {
          c = (short) (Short.toUnsignedInt(c) << 1);
        }
      }
      crc[0] = (short) (Short.toUnsignedInt(c) ^ Short.toUnsignedInt(crc[0]) << 8);
    }
    int crcInt = crc[0]; // & 0xffff;
    return ~crcInt;
  }

  class FIG0_HEADER {
    boolean cn;
    boolean oe;
    boolean pd;
    int extension;

    public FIG0_HEADER(byte data) {
      BitSet bitSet = BitSet.valueOf(new byte[] {data});
      cn = bitSet.get(7);
      oe = bitSet.get(6);
      pd = bitSet.get(5);
      BitSet extensionBits = bitSet.get(0, 5);
      extension = getIntFromBitSet(extensionBits);
    }

    private int getIntFromBitSet(BitSet bitSet) {
      int value = 0;
      for (int i = 0; i < bitSet.length(); i++) {
        if (bitSet.get(i)) {
          value += Math.pow(2, i);
        }
      }
      return value;
    }
  }

  class AUDIO_SERVICE {
    private int subchid;
    private boolean dab_plus;
    public static final int subchid_none = -1;

    public AUDIO_SERVICE() {
      this(subchid_none, false);
    }

    public AUDIO_SERVICE(int subchid, boolean dab_plus) {
      this.subchid = subchid;
      this.dab_plus = dab_plus;
    }

    public boolean IsNone() {
      return subchid == subchid_none;
    }

    public boolean equals(AUDIO_SERVICE audio_service) {
      return subchid == audio_service.subchid && dab_plus == audio_service.dab_plus;
    }

    public boolean notEquals(AUDIO_SERVICE audio_service) {
      return !equals(audio_service);
    }
  }

  class FIC_ASW_CLUSTER {
    private short asw_flags;
    private int subchid;
    public static final int asw_flags_none = 0x0000;
    public static final int subchid_none = -1;

    public boolean IsNone() {
      return subchid == subchid_none;
    }

    public FIC_ASW_CLUSTER() {
      asw_flags = asw_flags_none;
      subchid = subchid_none;
    }

    public boolean equals(FIC_ASW_CLUSTER fic_asw_cluster) {
      return asw_flags == fic_asw_cluster.asw_flags && subchid == fic_asw_cluster.subchid;
    }

    public boolean notEquals(FIC_ASW_CLUSTER fic_asw_cluster) {
      return !equals(fic_asw_cluster);
    }
  }

  class FIC_LABEL implements Cloneable {
    private int charset;
    private byte[] label;
    private short short_label_mask;
    public static final int charset_none = -1;

    public FIC_LABEL() {
      charset = charset_none;
      short_label_mask = 0x0000;
      label = new byte[16];
      Arrays.fill(label, (byte) 0x00);
    }

    public boolean IsNone() {
      return charset == charset_none;
    }

    public boolean equals(FIC_LABEL fic_label) {
      return charset == fic_label.charset
          && Arrays.equals(label, fic_label.label)
          && short_label_mask == fic_label.short_label_mask;
    }

    public boolean notEquals(FIC_LABEL fic_label) {
      return !equals(fic_label);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
      return super.clone();
    }
  }

  class FIC_ENSEMBLE implements Cloneable {
    private int eid;
    private FIC_LABEL label;
    private int ecc;
    private int lto;
    private int inter_table_id;
    private Map<Byte, FIC_ASW_CLUSTER> asw_clusters;
    public static final int eid_none = -1;
    public static final int ecc_none = -1;
    public static final int lto_none = -100;
    public static final int inter_table_id_none = -1;

    public FIC_ENSEMBLE() {
      this.eid = eid_none;
      this.ecc = ecc_none;
      this.lto = lto_none;
      this.inter_table_id = inter_table_id_none;
      asw_clusters = new HashMap<>();
    }

    public boolean IsNone() {
      return eid == eid_none;
    }

    public boolean equals(FIC_ENSEMBLE ensemble) {
      return this.eid == ensemble.eid
          && Objects.equals(this.label, ensemble.label)
          && this.ecc == ensemble.ecc
          && this.lto == ensemble.lto
          && this.inter_table_id == ensemble.inter_table_id
          && Objects.equals(this.asw_clusters, ensemble.asw_clusters);
    }

    public boolean notEquals(FIC_ENSEMBLE ensemble) {
      return !this.equals(ensemble);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
      // return super.clone();
      FIC_ENSEMBLE e = (FIC_ENSEMBLE) super.clone();
      if (e.label != null) e.label = (FIC_LABEL) e.label.clone();
      return e;
    }
  }

  void ProcessFIG0(byte[] data, int len) {
    if (len < 1) {
      System.err.println("FICDecoder: received empty FIG 0");
      return;
    }

    FIG0_HEADER header = new FIG0_HEADER(data[0]);
    data = Arrays.copyOfRange(data, 1, len);
    len--;

    if (header.cn || header.oe || header.pd) return;

    switch (header.extension) {
      case 1:
        ProcessFIG0_1(data, len);
        break;
      case 2:
        ProcessFIG0_2(data, len);
        break;
      case 5:
        ProcessFIG0_5(data, len);
        break;
      case 8:
        ProcessFIG0_8(data, len);
        break;
      case 9:
        ProcessFIG0_9(data, len);
        break;
      case 10:
        // FIG 0/10 - Date and time (d&t)
        // ProcessFIG0_10(data, len);
        break;
      case 13:
        // ProcessFIG0_13(data, len);
        break;
      case 17:
        // ProcessFIG0_17(data, len);
        break;
      case 18:
        // ProcessFIG0_18(data, len);
        break;
      case 19:
        // ProcessFIG0_19(data, len);
        break;
    }
  }

  void ProcessFIG0_1(byte[] data, int len) {
    // FIG 0/1 - Basic sub-channel organization

    // iterate through all sub-channels
    for (int offset = 0; offset < len; ) {
      int subchid = data[offset] >> 2;
      int start_address = (data[offset] & 0x03) << 8 | data[offset + 1];
      offset += 2;
      FIC_SUBCHANNEL sc = new FIC_SUBCHANNEL();
      sc.start = start_address;
      boolean short_long_form = (data[offset] & 0x80) != 0;
      if (short_long_form) {
        // long form
        int option = (data[offset] & 0x70) >> 4;
        int pl = (data[offset] & 0x0C) >> 2;
        int subch_size = (data[offset] & 0x03) << 8 | data[offset + 1];
        switch (option) {
          case 0b000:
            sc.size = subch_size;
            sc.pl = "EEP " + (pl + 1) + "-A";
            sc.bitrate = subch_size / eep_a_size_factors[pl] * 8;
            break;
          case 0b001:
            sc.size = subch_size;
            sc.pl = "EEP " + (pl + 1) + "-B";
            sc.bitrate = subch_size / eep_b_size_factors[pl] * 32;
            break;
        }
        offset += 2;
      } else {
        // short form
        boolean table_switch = (data[offset] & 0x40) != 0;
        if (!table_switch) {
          int table_index = data[offset] & 0x3F;
          sc.size = uep_sizes[table_index];
          sc.pl = "UEP " + uep_pls[table_index];
          sc.bitrate = uep_bitrates[table_index];
        }
        offset++;
      }

      if (!sc.IsNone()) {
        FIC_SUBCHANNEL current_sc = GetSubchannel(subchid);
        if (current_sc != null) sc.language = current_sc.language;
        if (current_sc == null || !current_sc.equals(sc)) {
          current_sc = sc;
          Logger.d(
              String.format(
                  "FICDecoder: SubChId %2d: start %3d CUs, size %3d CUs, PL %-7s = %3d kBit/s\n",
                  subchid, sc.start, sc.size, sc.pl, sc.bitrate));
          UpdateSubchannel(subchid);
        }
      }
    }
  }

  void ProcessFIG0_2(byte[] data, int len) {
    // FIG 0/2 - Basic service and service component definition
    // programme services only

    // iterate through all services
    int offset = 0;
    while (offset < len) {
      int countryId = (data[offset] >> 4); // upper left 4 bits
      int sid = (data[offset] << 8) | data[offset + 1];

      offset += 2;
      int num_service_comps = data[offset++] & 0x0F; // lower right 4 bits
      // iterate through all service components
      for (int comp = 0; comp < num_service_comps; comp++) {
        int tmid = data[offset] >> 6; // upper left 2 bits
        switch (tmid) {
          case 0b00:
            int ascty = data[offset] & 0x3F; // lower right 6 bits
            int subchid = data[offset + 1] >> 2; // upper left 6 bits
            boolean ps = (data[offset + 1] & 0x02) != 0; // lower right 1 bit
            boolean ca = (data[offset + 1] & 0x01) != 0; // lower right 1 bit
            if (!ca) {
              switch (ascty) {
                case 0:
                case 63:
                  boolean dab_plus = ascty == 63;
                  AUDIO_SERVICE audio_service = new AUDIO_SERVICE(subchid, dab_plus);
                  FIC_SERVICE service = GetService(sid);
                  AUDIO_SERVICE current_audio_service = service.audio_comps.get(subchid);
                  if (current_audio_service == null
                      || !current_audio_service.equals(audio_service)
                      || ps != (service.pri_comp_subchid == subchid)) {
                    current_audio_service = audio_service;
                    if (ps) {
                      service.pri_comp_subchid = subchid;
                    }
                    Logger.d("FICDecoder: CountryId " + countryId);
                    Logger.d(
                        String.format(
                            "FICDecoder: SId 0x%04X: audio service (SubChId %2d, %-4s, %s), CountryId %2d\n",
                            sid,
                            subchid,
                            dab_plus ? "DAB+" : "DAB",
                            ps ? "primary" : "secondary",
                            countryId));
                    UpdateService(service);
                  }
                  break;
              }
            }
        }
        offset += 2;
      }
    }
  }

  void ProcessFIG0_5(byte[] data, int len) {
    // FIG 0/5 - Service component language
    // programme services only

    // iterate through all components
    for (int offset = 0; offset < len; ) {
      boolean ls_flag = (data[offset] & 0x80) != 0;
      if (ls_flag) {
        // long form - skipped, as not relevant
        offset += 3;
      } else {
        boolean msc_fic_flag = (data[offset] & 0x40) != 0;
        if (!msc_fic_flag) {
          int subchid = data[offset] & 0x3F;
          int language = data[offset + 1];
          FIC_SUBCHANNEL current_sc = GetSubchannel(subchid);
          if (current_sc == null) current_sc = new FIC_SUBCHANNEL();
          if (current_sc.language != language) {
            current_sc.language = language;
            Logger.d(
                String.format(
                    "FICDecoder: SubChId %2d: language '%s'\n",
                    subchid, ConvertLanguageToString(language)));
            UpdateSubchannel(subchid);
          }
        }
        offset += 2;
      }
    }
  }

  void ProcessFIG0_8(byte[] data, int len) {
    // FIG 0/8 - Service component global definition
    // programme services only

    // iterate through all service components
    int offset = 0;
    while (offset < len) {
      int sid = (data[offset] << 8) | data[offset + 1];

      offset += 2;
      boolean ext_flag = (data[offset] & 0x80) != 0;
      int scids = data[offset] & 0x0F;
      offset++;
      boolean ls_flag = (data[offset] & 0x80) != 0;
      if (ls_flag) {
        // long form - skipped, as not relevant
        offset += 2;
      } else {
        // short form
        boolean msc_fic_flag = (data[offset] & 0x40) != 0;

        // handle only MSC components
        if (!msc_fic_flag) {
          int subchid = data[offset] & 0x3F;
          FIC_SERVICE service = GetService(sid);
          boolean new_comp = !service.comp_defs.containsKey(scids);
          int current_subchid = service.comp_defs.getOrDefault(scids, 0);
          if (new_comp || current_subchid != subchid) {
            service.comp_defs.put(scids, subchid);
            Logger.d(
                String.format(
                    "FICDecoder: SId 0x%04X, SCIdS %2d: MSC service component (SubChId %2d)\n",
                    sid, scids, subchid));
            UpdateService(service);
          }
        }
        offset++;
      }
      if (ext_flag) {
        offset++;
      }
    }
  }

  public String ConvertLanguageToString(int value) {
    if (value >= 0x00 && value <= 0x2B) return languages_0x00_to_0x2B[value];
    if (value == 0x40) return "background sound/clean feed";
    if (value >= 0x45 && value <= 0x7F) return languages_0x7F_downto_0x45[0x7F - value];
    return "unknown (" + Integer.toString(value) + ")";
  }

  private FIC_SUBCHANNEL GetSubchannel(int subchid) {
    // created automatically, if not yet existing
    return subchannels.get(subchid);
  }

  public void UpdateSubchannel(int subchid) {
    // update services that consist of this sub-channel
    for (Map.Entry<Integer, FIC_SERVICE> entry : services.entrySet()) {
      FIC_SERVICE s = entry.getValue();
      if (s.audio_comps.containsKey(subchid)) {
        UpdateService(s);
      }
    }
  }

  final int[] eep_a_size_factors = {12, 8, 6, 4};
  final int[] eep_b_size_factors = {27, 21, 18, 15};

  final int[] uep_sizes = {
    16, 21, 24, 29, 35, 24, 29, 35, 42, 52, 29, 35, 42, 52, 32, 42, 48, 58, 70, 40, 52, 58, 70, 84,
    48, 58, 70, 84, 104, 58, 70, 84, 104, 64, 84, 96, 116, 140, 80, 104, 116, 140, 168, 96, 116,
    140, 168, 208, 116, 140, 168, 208, 232, 128, 168, 192, 232, 280, 160, 208, 280, 192, 280, 416
  };
  final int[] uep_pls = {
    5, 4, 3, 2, 1, 5, 4, 3, 2, 1, 5, 4, 3, 2, 5, 4,
    3, 2, 1, 5, 4, 3, 2, 1, 5, 4, 3, 2, 1, 5, 4, 3,
    2, 5, 4, 3, 2, 1, 5, 4, 3, 2, 1, 5, 4, 3, 2, 1,
    5, 4, 3, 2, 1, 5, 4, 3, 2, 1, 5, 4, 2, 5, 3, 1
  };
  final int[] uep_bitrates = {
    32, 32, 32, 32, 32, 48, 48, 48, 48, 48, 56, 56, 56, 56, 64, 64, 64, 64, 64, 80, 80, 80, 80, 80,
    96, 96, 96, 96, 96, 112, 112, 112, 112, 128, 128, 128, 128, 128, 160, 160, 160, 160, 160, 192,
    192, 192, 192, 192, 224, 224, 224, 224, 224, 256, 256, 256, 256, 256, 320, 320, 320, 384, 384,
    384
  };

  final String[] languages_0x00_to_0x2B = {
    "unknown/not applicable", "Albanian", "Breton", "Catalan", "Croatian", "Welsh", "Czech",
        "Danish",
    "German", "English", "Spanish", "Esperanto", "Estonian", "Basque", "Faroese", "French",
    "Frisian", "Irish", "Gaelic", "Galician", "Icelandic", "Italian", "Sami", "Latin",
    "Latvian", "Luxembourgian", "Lithuanian", "Hungarian", "Maltese", "Dutch", "Norwegian",
        "Occitan",
    "Polish", "Portuguese", "Romanian", "Romansh", "Serbian", "Slovak", "Slovene", "Finnish",
    "Swedish", "Turkish", "Flemish", "Walloon"
  };
  final String[] languages_0x7F_downto_0x45 = {
    "Amharic", "Arabic", "Armenian", "Assamese", "Azerbaijani", "Bambora", "Belorussian", "Bengali",
    "Bulgarian", "Burmese", "Chinese", "Chuvash", "Dari", "Fulani", "Georgian", "Greek",
    "Gujurati", "Gurani", "Hausa", "Hebrew", "Hindi", "Indonesian", "Japanese", "Kannada",
    "Kazakh", "Khmer", "Korean", "Laotian", "Macedonian", "Malagasay", "Malaysian", "Moldavian",
    "Marathi", "Ndebele", "Nepali", "Oriya", "Papiamento", "Persian", "Punjabi", "Pushtu",
    "Quechua", "Russian", "Rusyn", "Serbo-Croat", "Shona", "Sinhalese", "Somali", "Sranan Tongo",
    "Swahili", "Tadzhik", "Tamil", "Tatar", "Telugu", "Thai", "Ukranian", "Urdu",
    "Uzbek", "Vietnamese", "Zulu"
  };

  class FIC_SERVICE {
    public int sid;
    public int pri_comp_subchid;
    public FIC_LABEL label;
    public int pty_static;
    public int pty_dynamic;
    public short asu_flags;
    public Set<Byte> cids;
    public Map<Integer, AUDIO_SERVICE> audio_comps;
    public Map<Integer, Integer> comp_defs;
    public Map<Integer, FIC_LABEL> comp_labels;
    public Map<Integer, List<Byte>> comp_sls_uas;

    public static final int sid_none = -1;

    public boolean IsNone() {
      return sid == sid_none;
    }

    public static final int pri_comp_subchid_none = -1;

    public boolean HasNoPriCompSubchid() {
      return pri_comp_subchid == pri_comp_subchid_none;
    }

    public static final int pty_none = -1;

    public static final short asu_flags_none = 0x0000;

    public FIC_SERVICE() {
      sid = sid_none;
      pri_comp_subchid = pri_comp_subchid_none;
      pty_static = pty_none;
      pty_dynamic = pty_none;
      asu_flags = asu_flags_none;
      audio_comps = new HashMap<>();
      comp_defs = new HashMap<>();
      comp_labels = new HashMap<>();
      comp_sls_uas = new HashMap<>();
      cids = new HashSet<>();
      label = new FIC_LABEL();
    }
  }

  class FIC_SUBCHANNEL {
    public int start;
    public int size;
    public String pl;
    public int bitrate;
    public int language;
    public static final int language_none = -1;

    public boolean IsNone() {
      return pl.isEmpty() && language == language_none;
    }

    public FIC_SUBCHANNEL() {
      start = 0;
      size = 0;
      bitrate = -1;
      language = language_none;
    }

    public boolean equals(FIC_SUBCHANNEL fic_subchannel) {
      return start == fic_subchannel.start
          && size == fic_subchannel.size
          && Objects.equals(pl, fic_subchannel.pl)
          && bitrate == fic_subchannel.bitrate
          && language == fic_subchannel.language;
    }

    public boolean notEquals(FIC_SUBCHANNEL fic_subchannel) {
      return !equals(fic_subchannel);
    }
  }

  class LISTED_SERVICE {
    private int sid;
    private int scids;
    private FIC_SUBCHANNEL subchannel;
    private AUDIO_SERVICE audio_service;
    private FIC_LABEL label;
    private int pty_static;
    private int pty_dynamic;
    private int sls_app_type;
    private short asu_flags;
    public Set<Byte> cids;
    private int pri_comp_subchid; // only used for sorting
    private boolean multi_comps;

    public static final int sidNone = -1;

    public boolean isNone() {
      return sid == sidNone;
    }

    public static final int scids_none = -1;

    public boolean isPrimary() {
      return scids == scids_none;
    }

    public static final int pty_none = -1;

    public boolean isNone(int pty) {
      return pty == pty_none;
    }

    public static final int asu_flags_none = 0x0000;

    public boolean isNone(short asu_flags) {
      return asu_flags == asu_flags_none;
    }

    public static final int sls_app_type_none = -1;

    public boolean hasSLS() {
      return sls_app_type != sls_app_type_none;
    }

    public LISTED_SERVICE() {
      cids = new HashSet<>();
      sid = sidNone;
      scids = scids_none;
      pty_static = pty_none;
      pty_dynamic = pty_none;
      sls_app_type = sls_app_type_none;
      asu_flags = asu_flags_none;
      pri_comp_subchid = AUDIO_SERVICE.subchid_none;
      multi_comps = false;
    }

    public boolean compareTo(LISTED_SERVICE service) {
      if (pri_comp_subchid != service.pri_comp_subchid) {
        return pri_comp_subchid < service.pri_comp_subchid;
      }
      if (sid != service.sid) {
        return sid < service.sid;
      }
      return scids < service.scids;
    }
  }

  void ProcessFIG0_9(byte[] data, int len) {
    // FIG 0/9 - Time and country identifier - Country, LTO and International table
    // ensemble ECC/LTO and international table ID only

    if (len < 3) return;
    try {
      FIC_ENSEMBLE new_ensemble = (FIC_ENSEMBLE) ensemble.clone();
      new_ensemble.lto = (data[0] & 0x20) != 0 ? -1 : 1 * (data[0] & 0x1F); // local time offset
      new_ensemble.ecc = data[1]; // extended country codes
      new_ensemble.inter_table_id = data[2]; // select which table used for programme types
      if (!ensemble.equals(new_ensemble)) {
        ensemble = new_ensemble;
        Logger.d(
            String.format(
                "FICDecoder: ECC: 0x%02X, LTO: %s, international table ID: 0x%02X (%s)\n",
                ensemble.ecc,
                ConvertLTOToString(ensemble.lto),
                ensemble.inter_table_id,
                ConvertInterTableIDToString(ensemble.inter_table_id)));
        UpdateEnsemble();
        for (Map.Entry<Integer, FIC_SERVICE> service : services.entrySet()) {
          FIC_SERVICE s = service.getValue();
          if (s.pty_static != FIC_SERVICE.pty_none || s.pty_dynamic != FIC_SERVICE.pty_none)
            UpdateService(s);
        }
      }
    } catch (CloneNotSupportedException e) {
      Logger.e("FICDecoder: Error cloning FIC_ENSEMBLE", e);
    }
  }

  private void UpdateEnsemble() {
    // abort update, if label not yet present
    if (ensemble.label == null || ensemble.label.IsNone()) return;

    // forward to observer
    // observer->FICChangeEnsemble(ensemble);
  }

  public static String ConvertLTOToString(int value) {
    char[] lto_string = new char[7];
    Formatter formatter = new Formatter();
    formatter.format("%+03d:%02d", value / 2, (value % 2) != 0 ? 30 : 0);
    String result = formatter.toString();
    formatter.close();
    return result;
  }

  public String ConvertInterTableIDToString(int value) {
    switch (value) {
      case 0x01:
        return "RDS PTY";
      case 0x02:
        return "RBDS PTY";
      default:
        return "unknown";
    }
  }

  public void UpdateService(FIC_SERVICE service) {
    if (service == null || service.HasNoPriCompSubchid() || service.label.IsNone()) {
      return;
    }
    boolean multi_comps = false;
    for (Map.Entry<Integer, Integer> comp_def : service.comp_defs.entrySet()) {
      if (comp_def.getValue() == service.pri_comp_subchid
          || !service.audio_comps.containsKey(comp_def.getValue())) {
        continue;
      }
      UpdateListedService(service, comp_def.getKey(), true);
      multi_comps = true;
    }
    UpdateListedService(service, LISTED_SERVICE.scids_none, multi_comps);
  }

  void UpdateListedService(FIC_SERVICE service, int scids, boolean multi_comps) {
    // assemble listed service
    LISTED_SERVICE ls = new LISTED_SERVICE();

    ls.sid = service.sid;
    ls.scids = scids;
    ls.label = service.label;
    ls.pty_static = service.pty_static;
    ls.pty_dynamic = service.pty_dynamic;
    ls.asu_flags = service.asu_flags;
    ls.cids = service.cids;
    ls.pri_comp_subchid = service.pri_comp_subchid;
    ls.multi_comps = multi_comps;

    if (scids == LISTED_SERVICE.scids_none) {
      // primary component
      ls.audio_service = service.audio_comps.get(service.pri_comp_subchid);
    } else {
      // secondary component
      ls.audio_service = service.audio_comps.get(service.comp_defs.get(scids));

      // use component label, if available
      for (Map.Entry<Integer, FIC_LABEL> cl_it : service.comp_labels.entrySet()) {
        if (cl_it.getKey() == scids) {
          ls.label = cl_it.getValue();
          break;
        }
      }
    }
    // use sub-channel information, if available
    for (Map.Entry<Integer, FIC_SUBCHANNEL> sc_it : subchannels.entrySet()) {
      if (sc_it.getKey() == ls.audio_service.subchid) {
        ls.subchannel = sc_it.getValue();
        break;
      }
    }
    /* check (for) Slideshow; currently only supported in X-PAD
     * - derive the required SCIdS (if not yet known)
     * - derive app type from UA data (if present)
     */
    int sls_scids = scids;
    if (sls_scids == LISTED_SERVICE.scids_none) {
      for (Map.Entry<Integer, Integer> comp_def : service.comp_defs.entrySet()) {
        if (comp_def.getValue() == ls.audio_service.subchid) {
          sls_scids = comp_def.getKey();
          break;
        }
      }
    }

    if (sls_scids != LISTED_SERVICE.scids_none && service.comp_sls_uas.containsKey(sls_scids)) {
      ls.sls_app_type = GetSLSAppType(service.comp_sls_uas.get(sls_scids));
    }

    // forward to observer
    // observer->FICChangeService(ls);
  }

  public int GetSLSAppType(List<Byte> ua_data) {

    boolean ca_flag = false;
    int xpad_app_type = 12;
    boolean dg_flag = false;
    int dscty = 60;

    if (ua_data.size() >= 2) {
      ca_flag = (ua_data.get(0) & 0x80) != 0;
      xpad_app_type = ua_data.get(0) & 0x1F;
      dg_flag = (ua_data.get(1) & 0x80) != 0;
      dscty = ua_data.get(1) & 0x3F;
    }
    if (!ca_flag && !dg_flag && dscty == 60) {
      return xpad_app_type;
    } else {
      return LISTED_SERVICE.sls_app_type_none;
    }
  }

  class FIG1_HEADER {
    int charset;
    boolean oe;
    int extension;

    public FIG1_HEADER(byte data) {
      charset = (data >> 4) & 0xFF;
      int intOe = (data >> 3) & 0x1;
      oe = intOe == 1;
      extension = data & 0x7;
    }
  }

  public void ProcessFIG1(byte[] data, int len) {
    if (len < 1) {
      System.err.println("FICDecoder: received empty FIG 1");
      return;
    }

    FIG1_HEADER header = new FIG1_HEADER(data[0]);
    data = Arrays.copyOfRange(data, 1, len);
    len--;

    if (header.oe) return;

    int len_id = -1;
    switch (header.extension) {
      case 0: // ensemble
      case 1: // programme service
        len_id = 2;
        break;
      case 4: // service component
        // programme services only (P/D = 0)
        if ((data[0] & 0x80) != 0) return;
        len_id = 3;
        break;
      default:
        return;
    }

    int len_calced = len_id + 16 + 2;
    if (len != len_calced) {
      System.err.printf(
          "FICDecoder: received FIG 1/%d having %d field bytes (expected: %d)\n",
          header.extension, len, len_calced);
      return;
    }

    FIC_LABEL label = new FIC_LABEL();
    label.charset = header.charset;
    System.arraycopy(data, len_id, label.label, 0, 16);
    label.short_label_mask = (short) ((data[len_id + 16] << 8) | data[len_id + 17]);

    // handle extension
    switch (header.extension) {
      case 0:
        {
          short eid = (short) ((data[0] << 8) | data[1]);
          ProcessFIG1_0(eid, label);
          break;
        }
      case 1:
        {
          short sid = (short) ((data[0] << 8) | data[1]);
          ProcessFIG1_1(sid, label);
          break;
        }
      case 4:
        {
          int scids = data[0] & 0x0F;
          short sid = (short) ((data[1] << 8) | data[2]);
          ProcessFIG1_4(sid, scids, label);
          break;
        }
    }
  }

  void ProcessFIG1_0(int eid, FIC_LABEL label) {
    if (ensemble.eid != eid || !ensemble.label.equals(label)) {
      ensemble.eid = eid;
      ensemble.label = label;
      String label_str = ConvertLabelToUTF8(label, null);
      String short_label_str = DeriveShortLabelUTF8(label_str, label.short_label_mask);
      Logger.d(
          String.format(
              "FICDecoder: EId 0x%04X: ensemble label '%s' ('%s')\n",
              eid, label_str, short_label_str));
      UpdateEnsemble();
    }
  }

  void ProcessFIG1_1(short sid, FIC_LABEL label) {
    FIC_SERVICE service = GetService(sid);
    if (service == null) service = new FIC_SERVICE();
    if (!service.label.equals(label)) {
      service.label = label;
      String label_str = ConvertLabelToUTF8(label, null);
      String short_label_str = DeriveShortLabelUTF8(label_str, label.short_label_mask);
      Logger.d(
          String.format(
              "FICDecoder: SId 0x%04X: programme service label '%s' ('%s')\n",
              sid, label_str, short_label_str));
      UpdateService(service);
    }
  }

  void ProcessFIG1_4(short sid, int scids, FIC_LABEL label) {
    FIC_SERVICE service = GetService(sid);
    FIC_LABEL comp_label = service.comp_labels.get(scids);
    if (comp_label == null) comp_label = new FIC_LABEL();
    if (!comp_label.equals(label)) {
      comp_label = label;
      String label_str = ConvertLabelToUTF8(label, null);
      String short_label_str = DeriveShortLabelUTF8(label_str, label.short_label_mask);
      Logger.d(
          String.format(
              "FICDecoder: SId 0x%04X, SCIdS %2d: service component label '%s' ('%s')\n",
              sid, scids, label_str, short_label_str));
      UpdateService(service);
    }
  }

  public FIC_SERVICE GetService(int sid) {
    FIC_SERVICE result = services.get(sid);
    if (result == null) result = new FIC_SERVICE();
    if (result.IsNone()) result.sid = sid;
    return result;
  }

  String ConvertLabelToUTF8(FIC_LABEL label, String[] charset_name) {
    String result =
        ConvertTextToUTF8(label.label, label.label.length, label.charset, false, charset_name);

    int last_pos = result.lastIndexOf(' ');
    if (last_pos != -1) result = result.substring(0, last_pos + 1);
    return result;
  }

  public static String DeriveShortLabelUTF8(String long_label, short short_label_mask) {
    String short_label = "";
    for (int i = 0; i < long_label.length(); i++) { // consider discarded trailing spaces
      if ((short_label_mask & (0x8000 >> i)) != 0) {
        short_label += UTF8Substr(long_label, i, 1);
      }
    }
    return short_label;
  }

  public static int UTF8CharsLen(String s, int chars) {
    int result;
    for (result = 0; result < s.length(); result++) {
      if ((s.charAt(result) & 0xC0) != 0x80) {
        if (chars == 0) {
          break;
        }
        chars--;
      }
    }
    return result;
  }

  public static String UTF8Substr(String s, int pos, int count) {
    String result = s;
    result = result.substring(UTF8CharsLen(result, pos));
    result = result.substring(0, UTF8CharsLen(result, count));
    return result;
  }

  public static String ConvertTextToUTF8(
      byte[] data, int len, int charset, boolean mot, String[] charset_name) {
    List<Byte> cleaned_data = new ArrayList<>();
    for (int i = 0; i < len; i++) {
      switch (data[i]) {
        case 0x00:
        case 0x0A:
        case 0x0B:
        case 0x1F:
          continue;
        default:
          cleaned_data.add(data[i]);
      }
    }

    if (charset == 0b0000) {
      if (charset_name != null) charset_name[0] = "EBU Latin based";
      StringBuilder result = new StringBuilder();
      for (byte c : cleaned_data) result.append(ConvertCharEBUToUTF8(c));
      return result.toString();
    }
    if (charset == 0b0100 && mot)
      return ConvertStringIconvToUTF8(cleaned_data, charset_name, "ISO-8859-1");
    if (charset == 0b0110 && !mot)
      return ConvertStringIconvToUTF8(cleaned_data, charset_name, "UCS-2BE");
    if (charset == 0b1111) {
      if (charset_name != null) charset_name[0] = "UTF-8";
      byte[] resultBytes = new byte[cleaned_data.size()];
      for (int i = 0; i < cleaned_data.size(); i++) resultBytes[i] = cleaned_data.get(i);
      return new String(resultBytes);
    }

    System.err.printf(
        "CharsetTools: The %s charset %d is not supported; ignoring!\n",
        mot ? "MOT" : "DAB", charset);
    return "";
  }

  public static String ConvertCharEBUToUTF8(final byte value) {
    if (value <= 0x1F) {
      return ebu_values_0x00_to_0x1F[value];
    }
    if (value >= 0x7B) {
      return ebu_values_0x7B_to_0xFF[value];
    }

    switch (value) {
      case 0x24:
        return "\u0142";
      case 0x5C:
        return "\u016E";
      case 0x5E:
        return "\u0141";
      case 0x60:
        return "\u0104";
    }

    return new String(new byte[] {value}, StandardCharsets.UTF_8);
  }

  public static String ConvertStringIconvToUTF8(
      List<Byte> cleaned_data, String[] charset_name, String src_charset) {
    Charset charset = Charset.forName(src_charset);
    byte[] input_bytes = new byte[cleaned_data.size()];
    for (int i = 0; i < cleaned_data.size(); i++) {
      input_bytes[i] = cleaned_data.get(i);
    }
    byte[] output_bytes = new byte[input_bytes.length * 4];
    int input_len = input_bytes.length;
    int output_len = output_bytes.length;
    int output_len_orig = output_len;

    try {
      String input = new String(input_bytes, charset);
      String output = new String(output_bytes, StandardCharsets.UTF_8);
      output_len = output.getBytes(StandardCharsets.UTF_8).length;

      if (charset_name != null) {
        charset_name[0] = src_charset;
      }
      return output.substring(0, output_len_orig - output_len);
    } catch (Exception e) {
      System.err.println("CharsetTools: error while converting");
      return "";
    }
  }

  public static final String[] ebu_values_0x00_to_0x1F = {
    "", "\u0118", "\u012E", "\u0172", "\u0102", "\u0116", "\u010E", "\u0218", "\u021A", "\u010A",
    "", "", "\u0120", "\u0139", "\u017B", "\u0143", "\u0105", "\u0119", "\u012F", "\u0173",
    "\u0103", "\u0117", "\u010F", "\u0219", "\u021B", "\u010B", "\u0147", "\u011A", "\u0121",
    "\u013A", "\u017C", ""
  };

  // starting some chars earlier than 0x80
  public static final String[] ebu_values_0x7B_to_0xFF = {
    "\u00AB", "\u016F", "\u00BB", "\u013D", "\u0126", "\u00E1", "\u00E0", "\u00E9", "\u00E8",
    "\u00ED", "\u00EC", "\u00F3", "\u00F2", "\u00FA", "\u00F9", "\u00D1", "\u00C7", "\u015E",
    "\u00DF", "\u00A1", "\u0178", "\u00E2", "\u00E4", "\u00EA", "\u00EB", "\u00EE", "\u00EF",
    "\u00F4", "\u00F6", "\u00FB", "\u00FC", "\u00F1", "\u00E7", "\u015F", "\u011F", "\u0131",
    "\u00FF", "\u0136", "\u0145", "\u00A9", "\u0122", "\u011E", "\u011B", "\u0148", "\u0151",
    "\u0150", "\u20AC", "\u00A3", "\u0024", "\u0100", "\u0112", "\u012A", "\u016A", "\u0137",
    "\u0146", "\u013B", "\u0123", "\u013C", "\u0130", "\u0144", "\u0171", "\u0170", "\u00BF",
    "\u013E", "\u00B0", "\u0101", "\u0113", "\u012B", "\u016B", "\u00C1", "\u00C0", "\u00C9",
    "\u00C8", "\u00CD", "\u00CC", "\u00D3", "\u00D2", "\u00DA", "\u00D9", "\u0158", "\u010C",
    "\u0160", "\u017D", "\u00D0", "\u013F", "\u00C2", "\u00C4", "\u00CA", "\u00CB", "\u00CE",
    "\u00CF", "\u00D4", "\u00D6", "\u00DB", "\u00DC", "\u0159", "\u010D", "\u0161", "\u017E",
    "\u0111", "\u0140", "\u00C3", "\u00C5", "\u00C6", "\u0152", "\u0177", "\u00DD", "\u00D5",
    "\u00D8", "\u00DE", "\u014A", "\u0154", "\u0106", "\u015A", "\u0179", "\u0164", "\u00F0",
    "\u00E3", "\u00E5", "\u00E6", "\u0153", "\u0175", "\u00FD", "\u00F5", "\u00F8", "\u00FE",
    "\u014B", "\u0155", "\u0107", "\u015B", "\u017A", "\u0165", "\u0127"
  };
}
