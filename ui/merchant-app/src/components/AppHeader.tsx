import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useTheme } from "../context/ThemeContext";
import { fonts } from "../theme";
export function BackHeader({ title, onBack }: {
    title: string;
    onBack: () => void;
}) { const { colors } = useTheme(); return <View style={{ flexDirection: "row", alignItems: "center", gap: 16 }}><Pressable accessibilityRole="button" accessibilityLabel="Go back" onPress={onBack} style={{ padding: 8 }}><Ionicons name="arrow-back" color={colors.primary} size={24}/></Pressable><Text style={{ fontFamily: fonts.bold, fontSize: 24, color: colors.primary }}>{title}</Text></View>; }
