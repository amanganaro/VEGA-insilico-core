package insilico.core.constant;

import java.io.Serializable;

public class InsilicoConstants implements Serializable {


        // For Training set molecules
        public static final short MOLECULE_UNKNOWN_SET = -1;
        public static final short MOLECULE_TRAINING =1;
        public static final short MOLECULE_TEST =2;

        // For descriptors and other numerical values
        public static final double MISSING_VALUE = -999;

        // Unique indices for Alert blocks
        public static final int SA_BLOCK_MUTAGEN_BENIGNI_BOSSA = 1;
        public static final int SA_BLOCK_MUTAGEN_SARPY = 2;
        public static final int SA_BLOCK_MUTAGEN_CRS4 = 3;
        public static final int SA_BLOCK_MUTAGEN_IRFMN = 4;
        public static final int SA_BLOCK_BCF_IRFMN = 5;
        public static final int SA_BLOCK_BCF_IRFMN_THRESHOLD = 6;
        public static final int SA_BLOCK_LOGP_MEYLAN = 7;
        public static final int SA_BLOCK_LOGP_MEYLAN_ADDITIONAL = 8;
        public static final int SA_BLOCK_LOGP_MEYLAN_CORRECTION = 9;
        public static final int SA_BLOCK_BCF_CAESAR = 10;
        public static final int SA_BLOCK_FISH_IRFMN = 11;
        public static final int SA_BLOCK_READY_BIO_IRFMN = 12;
        public static final int SA_BLOCK_MUTAGEN_BENIGNI_BOSSA_ADDITIONAL = 13;
        public static final int SA_BLOCK_CARC_IRFMN = 14;
        public static final int SA_BLOCK_PERSISTENCE_SEDIMENT_IRFMN = 15;
        public static final int SA_BLOCK_PERSISTENCE_SOIL_IRFMN = 16;
        public static final int SA_BLOCK_PERSISTENCE_WATER_IRFMN = 17;
        public static final int SA_BLOCK_CARC_ANTARES = 18;
        public static final int SA_BLOCK_CARC_ISSCANCGX = 19;
        public static final int SA_BLOCK_ESTROGEN_BIND_CERAPP = 20;
        public static final int SA_BLOCK_HEPATOTOXICITY = 21;
        public static final int SA_BLOCK_MUTAGEN_SARPY_18K = 22;
        public static final int SA_BLOCK_ANDROGEN_BIND_COMPARA_SARPY = 23;
        public static final int SA_BLOCK_REPROTOX_IRFMN = 24;
        public static final int SA_BLOCK_ALGAE_COMBASE = 25;
        public static final int SA_BLOCK_MICROBES_COMBASE = 26;
        public static final int SA_BLOCK_DAPHNIA_COMBASE = 27;
        public static final int SA_BLOCK_FISH_COMBASE = 28;
        public static final int SA_BLOCK_REPROTOX_VERMEER = 29;
        public static final int SA_BLOCK_SKIN_SENS_VERMEER = 30;
        public static final int SA_BLOCK_NEPHROTOX_VERMEER = 31;
        public static final int SA_BLOCK_FISH_ACUTE_VERMEER = 32;
        public static final int SA_BLOCK_DAPHNIA_VERMEER = 33;
        public static final int SA_BLOCK_CGX_VERMEER = 34;
        public static final int SA_BLOCK_MICRONUCLEUS_VERMEER = 35;
        public static final int SA_BLOCK_MICRONUCLEUS_INVITRO_MODEL = 36;
        public static final int SA_BLOCK_TODIVINE_BCF = 37;
        public static final int SA_BLOCK_TODIVINE_PERSISTENCE_WATER = 38;
        public static final int SA_BLOCK_TODIVINE_PERSISTENCE_SOIL = 39;
        public static final int SA_BLOCK_TODIVINE_PERSISTENCE_SEDIMENT = 40;
        public static final int SA_BLOCK_TODIVINE_ALGAE_ACUTE = 41;
        public static final int SA_BLOCK_TODIVINE_ALGAE_CHRONIC = 42;
        public static final int SA_BLOCK_TODIVINE_DAPHNIA_ACUTE = 43;
        public static final int SA_BLOCK_TODIVINE_DAPHNIA_CHRONIC = 44;
        public static final int SA_BLOCK_TODIVINE_FISH_ACUTE = 45;
        public static final int SA_BLOCK_TODIVINE_FISH_CHRONIC = 46;
        public static final int SA_BLOCK_TODIVINE_ANDROGEN = 47;
        public static final int SA_BLOCK_TODIVINE_ESTROGEN = 48;
        public static final int SA_BLOCK_MICRONUCLEUS_INVIVO = 49;
        public static final int SA_BLOCK_SKIN_SENS_NCSTOX = 50;
        public static final int SA_BLOCK_MOA_IRFMN = 51;
        public static final int SA_BLOCK_SKIN_IRR_CONCERT = 52;
        public static final int SA_BLOCK_DEVTOX_CONCERT = 53;
        public static final int SA_BLOCK_SKIN_SENS_CONCERT = 54;
        public static final int SA_BLOCK_SKIN_IRRITATION_CONCERT_B3 = 55;
        public static final int SA_BLOCK_EYE_IRRITATION_CONCERT = 56;

        // Property keys used for read-across in Alert objects
        public static final String KEY_ALERT_SHOW_SIMILAR_MOLS = "ra_sim";
        public static final String KEY_ALERT_IS_TOXIC = "ra_istox";
        public static final String KEY_ALERT_VALUE_ACCURACY = "ra_acc";
        public static final String KEY_ALERT_VALUE_ACCURACY_BIOCIDES = "ra_acc_bio";
        public static final String KEY_ALERT_VALUE_FISHER = "ra_fish";
        public static final String KEY_ALERT_VALUE_MEAN = "ra_mean";
        public static final String KEY_ALERT_VALUE_STDEV = "ra_std";
        public static final String KEY_ALERT_VALUE_HITS = "ra_hits";
        public static final String KEY_ALERT_IS_UPPER_THRESHOLD = "ra_thre";
        public static final String KEY_ALERT_IS_LOWER_THRESHOLD = "ra_low_thre";
        public static final String KEY_ALERT_BCF_CAESAR_OUTLIER = "bcf_out";

        public static final String KEY_ALERT_FISH_TOX_LESS_1 = "fish1";
        public static final String KEY_ALERT_FISH_TOX_1_10 = "fish10";
        public static final String KEY_ALERT_FISH_TOX_10_100 = "fish100";
        public static final String KEY_ALERT_FISH_TOX_OVER_100 = "fishover100";

        public static final String KEY_ALERT_ALGAE_TOX_LESS_1 = "algaelet1";
        public static final String KEY_ALERT_ALGAE_TOX_LESS_100 = "algaelet10";
        public static final String KEY_ALERT_ALGAE_TOX_GREATER_100 = "algaegt100";

        public static final String KEY_ALERT_MICROBES_TOX_LESS_100 = "microlet100";
        public static final String KEY_ALERT_MICROBES_TOX_LESS_1000 = "microlet1000";
        public static final String KEY_ALERT_MICROBES_TOX_GREATER_1000 = "microgt1000";

        public static final String KEY_ALERT_DAPHNIA_TOX_LESS_1 = "daphnialt1";
        public static final String KEY_ALERT_DAPHNIA_TOX_1_10 = "daphnia10";
        public static final String KEY_ALERT_DAPHNIA_TOX_10_100 = "daphnia100";
        public static final String KEY_ALERT_DAPHNIA_TOX_OVER_100 = "daphniaover100";

        public static final String KEY_ALERT_READY_BIO_NON_RB = "rb_non_ready";
        public static final String KEY_ALERT_READY_BIO_NON_RB_POSSIBLE = "rb_pos_non_ready";
        public static final String KEY_ALERT_READY_BIO_RB = "rb_ready";
        public static final String KEY_ALERT_READY_BIO_RB_POSSIBLE = "rb_pos_ready";

        public static final String KEY_ALERT_PERS_SEDIMENT_NP = "pers_sed_np";
        public static final String KEY_ALERT_PERS_SEDIMENT_VP = "pers_sed_vp";
        public static final String KEY_ALERT_PERS_WATER_NP = "pers_wat_np";
        public static final String KEY_ALERT_PERS_WATER_VP = "pers_wat_vp";
        public static final String KEY_ALERT_PERS_SOIL_NP = "pers_soi_np";
        public static final String KEY_ALERT_PERS_SOIL_VP = "pers_soi_vp";

        public static final String KEY_ALERT_ER_ACTIVE = "er_active";
        public static final String KEY_ALERT_ER_ACTIVE_POSSIBLE = "er_pos_active";
        public static final String KEY_ALERT_ER_INACTIVE = "er_inactive";
        public static final String KEY_ALERT_ER_INACTIVE_POSSIBLE = "er_pos_inactive";

        public static final String KEY_ALERT_HEPA_TOXIC = "he_tox";
        public static final String KEY_ALERT_HEPA_NONTOXIC = "he_nontox";
        public static final String KEY_ALERT_REPRO_TOXIC = "repro_tox";
        public static final String KEY_ALERT_REPRO_NONTOXIC = "repro_nontox";
        public static final String KEY_ALERT_NEPHRO_TOXIC = "nephro_tox";
        public static final String KEY_ALERT_NEPHRO_NONTOXIC = "nephro_nontox";
        public static final String KEY_ALERT_CARC_TOXIC = "carc_tox";
        public static final String KEY_ALERT_CARC_NONTOXIC = "carc_nontox";

        public static final String KEY_ALERT_SKIN_SENS = "skin_sen";
        public static final String KEY_ALERT_SKIN_NON_SENS = "skin_non_sen";

        public static final String KEY_ALERT_SKIN_IRR = "skin_irr";
        public static final String KEY_ALERT_SKIN_NON_IRR = "skin_non_irr";

        public static final String KEY_ALERT_EYE_IRR = "eye_irr";
        public static final String KEY_ALERT_EYE_NON_IRR = "eye_non_irr";

        public static final String KEY_ALERT_MICRONUCLEUS_ACTIVE = "mn_act";
        public static final String KEY_ALERT_MICRONUCLEUS_INACTIVE = "mn_inact";
        public static final String KEY_ALERT_MICRONUCLEUS_INVIVO_SA_BLOCK = "mn_alertblock";

        public static final String KEY_ALERT_AR_COMPARA_SARPY_ALL15_ACT_INF = "er_a15_ai";
        public static final String KEY_ALERT_AR_COMPARA_SARPY_ALL15_ACT_NONINF = "er_a15_an";
        public static final String KEY_ALERT_AR_COMPARA_SARPY_ALL15_INACT_INF = "er_a15_ii";
        public static final String KEY_ALERT_AR_COMPARA_SARPY_ALL15_INACT_NONINF = "er_a15_in";
        public static final String KEY_ALERT_AR_COMPARA_SARPY_ACT6 = "er_a6";

        public static final String KEY_ALERT_SARPY_STATS_INF = "sarpy_inf";
        public static final String KEY_ALERT_SARPY_STATS_LESS_THAN_INF = "sarpy_noninf";

        // Keys for reasoning items (AD)
        public static final short REASONING_UNCERTAINTY = 1;
        public static final short REASONING_DESCRIPTOR_ANALYSIS = 2;
        public static final short REASONING_ACF_ANALYSIS = 3;

        // Keys for acf (reasoning item)
        public static final short ACF_TYPE_RARE = 1;
        public static final short ACF_TYPE_MISSING = 2;
}
