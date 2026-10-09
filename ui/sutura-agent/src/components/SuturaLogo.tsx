import { Image, StyleSheet, type ImageStyle } from "react-native";
import { SUTURA_LOGO_URI } from "../constants/brand";

export function SuturaLogo({ size = 36, style }: { size?: number; style?: ImageStyle }) {
  return (
    <Image
      source={{ uri: SUTURA_LOGO_URI }}
      style={[styles.logo, { width: size, height: size }, style]}
      resizeMode="contain"
      accessibilityLabel="Sutura"
    />
  );
}

const styles = StyleSheet.create({
  logo: { aspectRatio: 1 },
});
