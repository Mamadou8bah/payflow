import {useTheme,useThemedStyles} from "../context/ThemeContext";
import type {ThemeColors} from "../theme";
import { Image, StyleSheet, type ImageStyle } from "react-native";
import { SUTURA_LOGO } from "../constants/brand";

export function SuturaLogo({ size = 36, style }: { size?: number; style?: ImageStyle }) {
 const {colors}=useTheme();
 const styles=useThemedStyles(createStyles);

  return (
    <Image
      source={SUTURA_LOGO}
      style={[styles.logo, { width: size, height: size }, style]}
      resizeMode="contain"
      accessibilityLabel="Sutura"
    />
  );
}

const createStyles = (colors: ThemeColors) => StyleSheet.create({
  logo: { backgroundColor: "#ffffff", borderRadius: 8, aspectRatio: 1 },
});
